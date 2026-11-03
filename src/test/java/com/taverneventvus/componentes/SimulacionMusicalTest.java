package com.taverneventvus.componentes;

import com.taverneventvus.bus.EventBus;
import com.taverneventvus.eventos.BandaTocandoEvent;
import com.taverneventvus.eventos.BebidaServidaEvent;
import com.taverneventvus.eventos.ComidaPreparadaEvent;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class SimulacionMusicalTest {
    @Test
    void musicaContinuaMientrasBarraYCocinaTrabajanYCierraTodosLosRecursos() throws Exception {
        EventBus bus = new EventBus();
        CountDownLatch preparaciones = new CountDownLatch(2);
        CountDownLatch liberar = new CountDownLatch(1);
        CountDownLatch canciones = new CountDownLatch(3);
        bus.suscribir(BebidaServidaEvent.class, evento -> esperar(preparaciones, liberar));
        bus.suscribir(ComidaPreparadaEvent.class, evento -> esperar(preparaciones, liberar));
        bus.suscribir(BandaTocandoEvent.class, evento -> canciones.countDown());
        var contenido = new ByteArrayOutputStream();
        Banda banda = new Banda(bus, "Los Seguidores de Dijkstra");
        Barra barra = new Barra(bus, Duration.ZERO);
        Cocina cocina = new Cocina(bus, Duration.ZERO);
        try (var salida = new PrintStream(contenido, true, StandardCharsets.UTF_8);
             PanelLED panel = new PanelLED(bus, salida, false);
             SistemaDeSonido sonido = new SistemaDeSonido(bus, salida)) {
            try {
                new SistemaDePedidos(bus).registrarPedido("mesa_5", "pedido_123",
                        List.of("cerveza", "hamburguesa"));
                assertTrue(preparaciones.await(3, TimeUnit.SECONDS));
                banda.iniciar("El Algoritmo del Amor", 180, Duration.ofMillis(10));
                assertTrue(canciones.await(3, TimeUnit.SECONDS), "La música no debe esperar a las preparaciones");
            } finally {
                liberar.countDown();
                cocina.close();
                barra.close();
                banda.close();
            }
        }
        assertTrue(barra.estaCerrada());
        assertTrue(cocina.estaCerrada());
        assertTrue(banda.estaDetenida());
        String texto = contenido.toString(StandardCharsets.UTF_8);
        assertTrue(texto.contains("[PANEL LED] Nuevo pedido recibido en mesa_5"));
        assertTrue(texto.contains("[PANEL LED] cerveza servida en mesa_5"));
        assertTrue(texto.contains("[PANEL LED] hamburguesa preparada para pedido_123"));
        assertTrue(texto.contains("[PANEL LED] Los Seguidores de Dijkstra están tocando El Algoritmo del Amor"));
        assertTrue(texto.contains("[SONIDO] Ajustando ecualizador para: El Algoritmo del Amor"));
        int longitud = contenido.size();
        bus.publicar(new BandaTocandoEvent("Banda", "Otra canción", 60));
        bus.publicar(new BebidaServidaEvent("mesa_6", "agua"));
        assertEquals(longitud, contenido.size());
    }

    private static void esperar(CountDownLatch preparaciones, CountDownLatch liberar) {
        preparaciones.countDown();
        try {
            if (!liberar.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("No se liberaron las preparaciones");
            }
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(error);
        }
    }
}

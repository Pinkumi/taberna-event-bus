package com.taverneventvus.componentes;

import com.taverneventvus.bus.EventBus;
import com.taverneventvus.eventos.*;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SistemaDeSonidoTest {
    @Test
    void soloReaccionaAMusicaYPermiteCancelarLaSuscripcion() {
        EventBus bus = new EventBus();
        var contenido = new ByteArrayOutputStream();
        try (var salida = new PrintStream(contenido, true, StandardCharsets.UTF_8)) {
            SistemaDeSonido sonido = new SistemaDeSonido(bus, salida);
            try (sonido) {
                bus.publicar(new PedidoRealizadoEvent("mesa_5", "pedido_123", List.of("cerveza")));
                bus.publicar(new BebidaServidaEvent("mesa_5", "cerveza"));
                bus.publicar(new ComidaPreparadaEvent("pedido_123", "hamburguesa"));
                assertEquals("", contenido.toString(StandardCharsets.UTF_8));
                bus.publicar(new BandaTocandoEvent("Banda", "El Algoritmo del Amor", 180));
            }
            bus.publicar(new BandaTocandoEvent("Banda", "Otra canción", 60));
            sonido.close();
            assertEquals("[SONIDO] Ajustando ecualizador para: El Algoritmo del Amor" + System.lineSeparator(),
                    contenido.toString(StandardCharsets.UTF_8));
        }
    }
}

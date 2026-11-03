package com.taverneventvus.componentes;

import com.taverneventvus.bus.EventBus;
import com.taverneventvus.eventos.BebidaServidaEvent;
import com.taverneventvus.eventos.ComidaPreparadaEvent;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class PreparacionParalelaTest {
    @Test
    void pedidoMixtoNoBloqueaYAmbasPreparacionesTrabajanEnParalelo() throws Exception {
        EventBus bus = new EventBus();
        CountDownLatch preparaciones = new CountDownLatch(2);
        CountDownLatch liberar = new CountDownLatch(1);
        var bebidas = new CopyOnWriteArrayList<BebidaServidaEvent>();
        var comidas = new CopyOnWriteArrayList<ComidaPreparadaEvent>();
        bus.suscribir(BebidaServidaEvent.class, evento -> {
            bebidas.add(evento);
            esperar(preparaciones, liberar);
        });
        bus.suscribir(ComidaPreparadaEvent.class, evento -> {
            comidas.add(evento);
            esperar(preparaciones, liberar);
        });
        Barra barra = new Barra(bus, Duration.ZERO);
        Cocina cocina = new Cocina(bus, Duration.ZERO);
        var publicador = Executors.newSingleThreadExecutor();
        try {
            var publicacion = publicador.submit(() -> new SistemaDePedidos(bus)
                    .registrarPedido("mesa_5", "pedido_123", List.of("cerveza", "hamburguesa")));
            publicacion.get(3, TimeUnit.SECONDS);
            assertTrue(preparaciones.await(3, TimeUnit.SECONDS), "Ambos hilos deben llegar sin esperarse");
        } finally {
            liberar.countDown();
            barra.close();
            cocina.close();
            publicador.shutdownNow();
            assertTrue(publicador.awaitTermination(3, TimeUnit.SECONDS));
        }
        assertTrue(barra.estaCerrada());
        assertTrue(cocina.estaCerrada());
        assertEquals(List.of(new BebidaServidaEvent("mesa_5", "cerveza")), bebidas);
        assertEquals(List.of(new ComidaPreparadaEvent("pedido_123", "hamburguesa")), comidas);
    }

    @Test
    void productosDesconocidosNoGeneranEventos() {
        EventBus bus = new EventBus();
        var recibidos = new CopyOnWriteArrayList<Object>();
        bus.suscribir(BebidaServidaEvent.class, recibidos::add);
        bus.suscribir(ComidaPreparadaEvent.class, recibidos::add);
        try (Barra barra = new Barra(bus, Duration.ZERO);
             Cocina cocina = new Cocina(bus, Duration.ZERO)) {
            new SistemaDePedidos(bus).registrarPedido("mesa_5", "pedido_123", List.of("poción", ""));
        }
        assertTrue(recibidos.isEmpty());
    }

    private static void esperar(CountDownLatch preparaciones, CountDownLatch liberar) {
        preparaciones.countDown();
        try {
            if (!liberar.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("La prueba no liberó la preparación");
            }
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(error);
        }
    }
}

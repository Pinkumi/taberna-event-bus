package com.taverneventvus.bus;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class EventBusTest {
    private final EventBus bus = new EventBus();

    @Test
    void notificaATodosLosSuscriptoresDelTipoExacto() {
        List<String> recibidos = new ArrayList<>();
        List<Integer> numeros = new ArrayList<>();
        List<Object> objetos = new ArrayList<>();
        bus.suscribir(String.class, recibidos::add);
        bus.suscribir(String.class, texto -> recibidos.add("Copia: " + texto));
        bus.suscribir(Integer.class, numeros::add);
        bus.suscribir(Object.class, objetos::add);

        bus.publicar("Pedido");
        bus.publicar(7);

        assertEquals(List.of("Pedido", "Copia: Pedido"), recibidos);
        assertEquals(List.of(7), numeros);
        assertTrue(objetos.isEmpty());
    }

    @Test
    void desuscribirEliminaSoloElSuscriptorRegistrado() {
        List<String> recibidos = new ArrayList<>();
        Consumer<String> suscriptor = recibidos::add;
        bus.suscribir(String.class, suscriptor);
        bus.suscribir(String.class, texto -> recibidos.add("Otro: " + texto));
        bus.publicar("Antes");
        bus.desuscribir(String.class, suscriptor);
        bus.publicar("Después");

        assertEquals(List.of("Antes", "Otro: Antes", "Otro: Después"), recibidos);
    }

    @Test
    void publicarSinSuscriptoresYDesuscribirAusentesNoGeneraErrores() {
        Consumer<String> suscriptor = texto -> fail("No debe recibir eventos");
        assertDoesNotThrow(() -> {
            bus.publicar("Sin suscriptores");
            bus.desuscribir(String.class, suscriptor);
            bus.suscribir(String.class, suscriptor);
            bus.desuscribir(String.class, suscriptor);
            bus.desuscribir(String.class, suscriptor);
            bus.publicar("Eliminado");
        });
    }

    @Test
    void permiteDesuscribirseDuranteLaNotificacion() {
        List<String> recibidos = new ArrayList<>();
        Consumer<String> suscriptor = new Consumer<>() {
            @Override
            public void accept(String texto) {
                recibidos.add(texto);
                bus.desuscribir(String.class, this);
            }
        };
        bus.suscribir(String.class, suscriptor);
        bus.publicar("Primero");
        bus.publicar("Segundo");
        assertEquals(List.of("Primero"), recibidos);
    }
}

package com.taverneventvus.componentes;

import com.taverneventvus.bus.EventBus;
import com.taverneventvus.eventos.BandaTocandoEvent;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class BandaTest {
    @Test
    void publicaLosDatosDeLaCancion() {
        EventBus bus = new EventBus();
        List<BandaTocandoEvent> eventos = new ArrayList<>();
        bus.suscribir(BandaTocandoEvent.class, eventos::add);
        try (Banda banda = new Banda(bus, "Los Seguidores de Dijkstra")) {
            banda.tocarCancion("El Algoritmo del Amor", 180);
        }
        assertEquals(List.of(new BandaTocandoEvent("Los Seguidores de Dijkstra",
                "El Algoritmo del Amor", 180)), eventos);
    }

    @Test
    void publicaPeriodicamenteYDetieneSuEjecutor() throws Exception {
        EventBus bus = new EventBus();
        var eventos = new CopyOnWriteArrayList<BandaTocandoEvent>();
        CountDownLatch publicaciones = new CountDownLatch(3);
        bus.suscribir(BandaTocandoEvent.class, evento -> {
            eventos.add(evento);
            publicaciones.countDown();
        });
        Banda banda = new Banda(bus, "Banda");
        try (banda) {
            banda.iniciar("Canción", 180, Duration.ofMillis(10));
            assertTrue(publicaciones.await(3, TimeUnit.SECONDS));
            assertThrows(IllegalStateException.class,
                    () -> banda.iniciar("Otra", 10, Duration.ofMillis(10)));
            banda.detener();
            assertTrue(banda.estaDetenida());
            assertTrue(eventos.size() >= 3);
            assertTrue(eventos.stream().allMatch(evento ->
                    evento.equals(new BandaTocandoEvent("Banda", "Canción", 180))));
            assertThrows(IllegalStateException.class, () -> banda.tocarCancion("Otra", 10));
        }
    }

    @Test
    void rechazaIntervalosSinDemoraPositiva() {
        try (Banda banda = new Banda(new EventBus(), "Banda")) {
            assertThrows(IllegalArgumentException.class,
                    () -> banda.iniciar("Canción", 180, Duration.ZERO));
            assertThrows(IllegalArgumentException.class,
                    () -> banda.iniciar("Canción", 180, Duration.ofMillis(-1)));
        }
    }
}

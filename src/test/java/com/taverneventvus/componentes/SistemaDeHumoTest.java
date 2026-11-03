package com.taverneventvus.componentes;

import com.taverneventvus.bus.EventBus;
import com.taverneventvus.eventos.BandaTocandoEvent;
import com.taverneventvus.eventos.BebidaServidaEvent;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SistemaDeHumoTest {
    @Test
    void activaHumoSoloConLaCancionExacta() {
        EventBus bus = new EventBus();
        var contenido = new ByteArrayOutputStream();
        try (var salida = new PrintStream(contenido, true, StandardCharsets.UTF_8);
             SistemaDeHumo humo = new SistemaDeHumo(bus, salida)) {
            bus.publicar(new BandaTocandoEvent("Banda", "El Algoritmo del Amor", 180));
            bus.publicar(new BandaTocandoEvent("Banda", "through the fire and flames", 440));
            bus.publicar(new BandaTocandoEvent("Banda", "Through the Fire and Flames (Live)", 440));
            bus.publicar(new BebidaServidaEvent("mesa_5", "cerveza"));
            assertEquals("", contenido.toString(StandardCharsets.UTF_8));
            bus.publicar(new BandaTocandoEvent("Banda", "Through the Fire and Flames", 440));
            assertEquals("[HUMO] Efectos de humo activados para: Through the Fire and Flames"
                    + System.lineSeparator(), contenido.toString(StandardCharsets.UTF_8));
        }
    }

    @Test
    void seIncorporaAUnaBandaExistenteYPermiteDesuscribirse() {
        EventBus bus = new EventBus();
        var contenido = new ByteArrayOutputStream();
        try (Banda banda = new Banda(bus, "Los Seguidores de Dijkstra");
             var salida = new PrintStream(contenido, true, StandardCharsets.UTF_8)) {
            banda.tocarCancion("Through the Fire and Flames", 440);
            SistemaDeHumo humo = new SistemaDeHumo(bus, salida);
            try (humo) {
                banda.tocarCancion("Through the Fire and Flames", 440);
            }
            humo.close();
            banda.tocarCancion("Through the Fire and Flames", 440);
            assertEquals("[HUMO] Efectos de humo activados para: Through the Fire and Flames"
                    + System.lineSeparator(), contenido.toString(StandardCharsets.UTF_8));
        }
    }
}

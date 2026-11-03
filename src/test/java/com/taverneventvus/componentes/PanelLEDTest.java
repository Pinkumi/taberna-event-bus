package com.taverneventvus.componentes;

import com.taverneventvus.bus.EventBus;
import com.taverneventvus.eventos.*;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PanelLEDTest {
    @Test
    void muestraCuatroTiposDeEventosYCancelaTodasSusSuscripciones() {
        EventBus bus = new EventBus();
        var contenido = new ByteArrayOutputStream();
        try (var salida = new PrintStream(contenido, true, StandardCharsets.UTF_8)) {
            PanelLED panel = new PanelLED(bus, salida, false);
            try (panel) {
                publicarEventos(bus);
            }
            String esperado = String.join(System.lineSeparator(),
                    "[PANEL LED] Nuevo pedido recibido en mesa_5",
                    "[PANEL LED] cerveza servida en mesa_5",
                    "[PANEL LED] hamburguesa preparada para pedido_123",
                    "[PANEL LED] Los Seguidores de Dijkstra están tocando El Algoritmo del Amor")
                    + System.lineSeparator();
            assertEquals(esperado, contenido.toString(StandardCharsets.UTF_8));
            publicarEventos(bus);
            panel.close();
            assertEquals(esperado, contenido.toString(StandardCharsets.UTF_8));
        }
    }

    @Test
    void permiteColoresAnsiSinPerderElTexto() {
        EventBus bus = new EventBus();
        var contenido = new ByteArrayOutputStream();
        try (var salida = new PrintStream(contenido, true, StandardCharsets.UTF_8);
             PanelLED panel = new PanelLED(bus, salida, true)) {
            bus.publicar(new BebidaServidaEvent("mesa_5", "cerveza"));
        }
        assertEquals("\u001B[34m[PANEL LED] cerveza servida en mesa_5\u001B[0m" + System.lineSeparator(),
                contenido.toString(StandardCharsets.UTF_8));
    }

    private static void publicarEventos(EventBus bus) {
        bus.publicar(new PedidoRealizadoEvent("mesa_5", "pedido_123", List.of("cerveza", "hamburguesa")));
        bus.publicar(new BebidaServidaEvent("mesa_5", "cerveza"));
        bus.publicar(new ComidaPreparadaEvent("pedido_123", "hamburguesa"));
        bus.publicar(new BandaTocandoEvent("Los Seguidores de Dijkstra", "El Algoritmo del Amor", 180));
    }
}

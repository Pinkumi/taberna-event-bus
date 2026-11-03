package com.taverneventvus.eventos;

import com.taverneventvus.bus.EventBus;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventosTest {
    @Test
    void pedidoConservaSusDatosYUnaCopiaInmutableDeLosProductos() {
        List<String> productos = new ArrayList<>(List.of("Cerveza", "Hamburguesa"));
        var evento = new PedidoRealizadoEvent("mesa_5", "pedido_123", productos);
        productos.clear();

        assertEquals("mesa_5", evento.mesaId());
        assertEquals("pedido_123", evento.pedidoId());
        assertEquals(List.of("Cerveza", "Hamburguesa"), evento.productos());
        assertThrows(UnsupportedOperationException.class, () -> evento.productos().add("Agua"));
        assertThrows(UnsupportedOperationException.class, () -> evento.productos().set(0, "Agua"));
        assertThrows(UnsupportedOperationException.class, () -> evento.productos().clear());
    }

    @Test
    void eventosConservanLaInformacionDeBebidasComidaYMusica() {
        var bebida = new BebidaServidaEvent("mesa_5", "Cerveza");
        var comida = new ComidaPreparadaEvent("pedido_123", "Hamburguesa");
        var banda = new BandaTocandoEvent("Los Seguidores de Dijkstra", "El Algoritmo del Amor", 180);

        assertEquals("mesa_5", bebida.mesaId());
        assertEquals("Cerveza", bebida.bebida());
        assertEquals("pedido_123", comida.pedidoId());
        assertEquals("Hamburguesa", comida.plato());
        assertEquals("Los Seguidores de Dijkstra", banda.nombreBanda());
        assertEquals("El Algoritmo del Amor", banda.nombreCancion());
        assertEquals(180, banda.duracion());
    }

    @Test
    void busDistribuyeLosCuatroEventosPorSuTipo() {
        EventBus bus = new EventBus();
        List<Object> recibidos = new ArrayList<>();
        bus.suscribir(PedidoRealizadoEvent.class, recibidos::add);
        bus.suscribir(BebidaServidaEvent.class, recibidos::add);
        bus.suscribir(ComidaPreparadaEvent.class, recibidos::add);
        bus.suscribir(BandaTocandoEvent.class, recibidos::add);
        var pedido = new PedidoRealizadoEvent("mesa_5", "pedido_123", List.of("Cerveza"));
        var bebida = new BebidaServidaEvent("mesa_5", "Cerveza");
        var comida = new ComidaPreparadaEvent("pedido_123", "Hamburguesa");
        var banda = new BandaTocandoEvent("Banda", "Canción", 180);

        bus.publicar(pedido);
        bus.publicar(bebida);
        bus.publicar(comida);
        bus.publicar(banda);

        assertEquals(List.of(pedido, bebida, comida, banda), recibidos);
    }
}

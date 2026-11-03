package com.taverneventvus.componentes;

import com.taverneventvus.bus.EventBus;
import com.taverneventvus.eventos.ComidaPreparadaEvent;
import com.taverneventvus.eventos.PedidoRealizadoEvent;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;

class CocinaTest {
    @Test
    void preparaSoloAlimentosYConservaPedidoYProducto() {
        EventBus bus = new EventBus();
        var recibidos = new CopyOnWriteArrayList<ComidaPreparadaEvent>();
        bus.suscribir(ComidaPreparadaEvent.class, recibidos::add);
        Cocina cocina = new Cocina(bus, Duration.ofMillis(10));
        try (cocina) {
            bus.publicar(new PedidoRealizadoEvent("mesa_5", "pedido_123",
                    List.of("cerveza", "hamburguesa", "PIZZA", "papas fritas", "desconocido")));
        }
        assertEquals(List.of(new ComidaPreparadaEvent("pedido_123", "hamburguesa"),
                new ComidaPreparadaEvent("pedido_123", "PIZZA"),
                new ComidaPreparadaEvent("pedido_123", "papas fritas")), recibidos);
        assertTrue(cocina.estaCerrada());
        bus.publicar(new PedidoRealizadoEvent("mesa_6", "pedido_124", List.of("pizza")));
        cocina.close();
        assertEquals(3, recibidos.size());
    }

    @Test
    void rechazaTiemposNegativos() {
        assertThrows(IllegalArgumentException.class,
                () -> new Cocina(new EventBus(), Duration.ofMillis(-1)));
    }
}

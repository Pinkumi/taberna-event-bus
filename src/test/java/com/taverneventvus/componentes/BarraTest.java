package com.taverneventvus.componentes;

import com.taverneventvus.bus.EventBus;
import com.taverneventvus.eventos.BebidaServidaEvent;
import com.taverneventvus.eventos.PedidoRealizadoEvent;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;

class BarraTest {
    @Test
    void preparaSoloBebidasYConservaMesaYProducto() {
        EventBus bus = new EventBus();
        var recibidos = new CopyOnWriteArrayList<BebidaServidaEvent>();
        bus.suscribir(BebidaServidaEvent.class, recibidos::add);
        Barra barra = new Barra(bus, Duration.ofMillis(10));
        try (barra) {
            bus.publicar(new PedidoRealizadoEvent("mesa_5", "pedido_123",
                    List.of("cerveza", "hamburguesa", " Agua ", "refresco", "desconocido")));
        }
        assertEquals(List.of(new BebidaServidaEvent("mesa_5", "cerveza"),
                new BebidaServidaEvent("mesa_5", " Agua "),
                new BebidaServidaEvent("mesa_5", "refresco")), recibidos);
        assertTrue(barra.estaCerrada());
        bus.publicar(new PedidoRealizadoEvent("mesa_6", "pedido_124", List.of("cerveza")));
        barra.close();
        assertEquals(3, recibidos.size());
    }

    @Test
    void rechazaTiemposNegativos() {
        assertThrows(IllegalArgumentException.class,
                () -> new Barra(new EventBus(), Duration.ofMillis(-1)));
    }
}

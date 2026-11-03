package com.taverneventvus.componentes;

import com.taverneventvus.bus.EventBus;
import com.taverneventvus.eventos.PedidoRealizadoEvent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SistemaDePedidosTest {
    @Test
    void publicaElPedidoConSusIdentificadoresYProductos() {
        EventBus bus = new EventBus();
        List<PedidoRealizadoEvent> recibidos = new ArrayList<>();
        bus.suscribir(PedidoRealizadoEvent.class, recibidos::add);
        List<String> productos = new ArrayList<>(List.of("cerveza", "hamburguesa"));

        new SistemaDePedidos(bus).registrarPedido("mesa_5", "pedido_123", productos);
        productos.clear();

        assertEquals(List.of(new PedidoRealizadoEvent("mesa_5", "pedido_123",
                List.of("cerveza", "hamburguesa"))), recibidos);
    }
}

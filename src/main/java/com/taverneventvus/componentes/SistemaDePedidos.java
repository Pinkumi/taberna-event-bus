package com.taverneventvus.componentes;

import com.taverneventvus.bus.EventBus;
import com.taverneventvus.eventos.PedidoRealizadoEvent;

import java.util.List;
import java.util.Objects;

public class SistemaDePedidos {
    private final EventBus bus;

    public SistemaDePedidos(EventBus bus) {
        this.bus = Objects.requireNonNull(bus);
    }

    public void registrarPedido(String mesaId, String pedidoId, List<String> productos) {
        var pedido = new PedidoRealizadoEvent(mesaId, pedidoId, productos);
        System.out.println("[Pedidos] Recibido " + pedidoId + " de " + mesaId + ": " + productos);
        bus.publicar(pedido);
        System.out.println("[Pedidos] Publicado PedidoRealizadoEvent: " + pedidoId);
    }
}

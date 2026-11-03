package com.taverneventvus.eventos;

import java.util.List;

public record PedidoRealizadoEvent(String mesaId, String pedidoId, List<String> productos) {
    public PedidoRealizadoEvent {
        productos = List.copyOf(productos);
    }
}

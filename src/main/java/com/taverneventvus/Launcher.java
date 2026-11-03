package com.taverneventvus;

import com.taverneventvus.bus.EventBus;
import com.taverneventvus.componentes.Barra;
import com.taverneventvus.componentes.Cocina;
import com.taverneventvus.componentes.SistemaDePedidos;

import java.util.List;

public class Launcher {
    public static void main(String[] args) {
        System.out.println("El Gólem Alquímico: Publisher/Subscriber con Event Bus.");
        EventBus bus = new EventBus();
        SistemaDePedidos pedidos = new SistemaDePedidos(bus);
        try (Barra barra = new Barra(bus); Cocina cocina = new Cocina(bus)) {
            pedidos.registrarPedido("mesa_5", "pedido_123", List.of("cerveza", "hamburguesa"));
            System.out.println("[Aplicación] Pedido publicado; esperando las preparaciones.");
        }
        System.out.println("[Aplicación] Preparaciones terminadas y recursos cerrados.");
    }
}

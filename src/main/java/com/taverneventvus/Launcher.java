package com.taverneventvus;

import com.taverneventvus.bus.EventBus;
import com.taverneventvus.componentes.Barra;
import com.taverneventvus.componentes.Cocina;
import com.taverneventvus.componentes.SistemaDePedidos;
import com.taverneventvus.componentes.Banda;
import com.taverneventvus.componentes.PanelLED;
import com.taverneventvus.componentes.SistemaDeSonido;
import com.taverneventvus.componentes.SistemaDeHumo;

import java.time.Duration;
import java.util.List;

public class Launcher {
    public static void main(String[] args) {
        System.out.println("El Gólem Alquímico: Publisher/Subscriber con Event Bus.");
        EventBus bus = new EventBus();
        SistemaDePedidos pedidos = new SistemaDePedidos(bus);
        try (PanelLED panel = new PanelLED(bus);
             SistemaDeSonido sonido = new SistemaDeSonido(bus);
             SistemaDeHumo humo = new SistemaDeHumo(bus);
             Banda banda = new Banda(bus, "Los Seguidores de Dijkstra");
             Barra barra = new Barra(bus);
             Cocina cocina = new Cocina(bus)) {
            pedidos.registrarPedido("mesa_5", "pedido_123", List.of("cerveza", "hamburguesa"));
            banda.tocarCancion("Through the Fire and Flames", 440);
            banda.iniciar("El Algoritmo del Amor", 180, Duration.ofSeconds(1));
            System.out.println("[Aplicación] Música y preparaciones en curso.");
        }
        System.out.println("[Aplicación] Preparaciones terminadas, banda detenida y recursos cerrados.");
    }
}

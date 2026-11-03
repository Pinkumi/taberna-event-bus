package com.taverneventvus.componentes;

import com.taverneventvus.bus.EventBus;
import com.taverneventvus.eventos.ComidaPreparadaEvent;
import com.taverneventvus.eventos.PedidoRealizadoEvent;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Consumer;

public class Cocina implements AutoCloseable {
    private final EventBus bus;
    private final PreparacionProgramada preparacion;
    private final Consumer<PedidoRealizadoEvent> suscriptor = this::prepararPedido;

    public Cocina(EventBus bus) {
        this(bus, Duration.ofSeconds(5));
    }

    public Cocina(EventBus bus, Duration tiempoPreparacion) {
        this.bus = Objects.requireNonNull(bus);
        preparacion = new PreparacionProgramada(tiempoPreparacion, "cocina");
        bus.suscribir(PedidoRealizadoEvent.class, suscriptor);
    }

    private void prepararPedido(PedidoRealizadoEvent pedido) {
        for (String producto : pedido.productos()) {
            if (CatalogoProductos.esAlimento(producto)) {
                System.out.println("[Cocina] Preparando " + producto + " para " + pedido.pedidoId());
                preparacion.programar(() -> {
                    System.out.println("[Cocina] Publicando ComidaPreparadaEvent: " + producto + " para " + pedido.pedidoId());
                    bus.publicar(new ComidaPreparadaEvent(pedido.pedidoId(), producto));
                });
            } else if (!CatalogoProductos.esConocido(producto)) {
                System.out.println("[Cocina] Producto desconocido: " + producto);
            }
        }
    }

    public boolean estaCerrada() {
        return preparacion.estaCerrada();
    }

    @Override
    public void close() {
        bus.desuscribir(PedidoRealizadoEvent.class, suscriptor);
        preparacion.close();
    }
}

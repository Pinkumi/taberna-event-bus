package com.taverneventvus.componentes;

import com.taverneventvus.bus.EventBus;
import com.taverneventvus.eventos.BebidaServidaEvent;
import com.taverneventvus.eventos.PedidoRealizadoEvent;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Consumer;

public class Barra implements AutoCloseable {
    private final EventBus bus;
    private final PreparacionProgramada preparacion;
    private final Consumer<PedidoRealizadoEvent> suscriptor = this::prepararPedido;

    public Barra(EventBus bus) {
        this(bus, Duration.ofSeconds(2));
    }

    public Barra(EventBus bus, Duration tiempoPreparacion) {
        this.bus = Objects.requireNonNull(bus);
        preparacion = new PreparacionProgramada(tiempoPreparacion, "barra");
        bus.suscribir(PedidoRealizadoEvent.class, suscriptor);
    }

    private void prepararPedido(PedidoRealizadoEvent pedido) {
        for (String producto : pedido.productos()) {
            if (CatalogoProductos.esBebida(producto)) {
                System.out.println("[Barra] Preparando " + producto + " para " + pedido.mesaId());
                preparacion.programar(() -> {
                    System.out.println("[Barra] Publicando BebidaServidaEvent: " + producto + " para " + pedido.mesaId());
                    bus.publicar(new BebidaServidaEvent(pedido.mesaId(), producto));
                });
            } else if (!CatalogoProductos.esConocido(producto)) {
                System.out.println("[Barra] Producto desconocido: " + producto);
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

package com.taverneventvus.componentes;

import com.taverneventvus.bus.EventBus;
import com.taverneventvus.eventos.BandaTocandoEvent;
import com.taverneventvus.eventos.BebidaServidaEvent;
import com.taverneventvus.eventos.ComidaPreparadaEvent;
import com.taverneventvus.eventos.PedidoRealizadoEvent;

import java.io.PrintStream;
import java.util.Objects;
import java.util.function.Consumer;

public class PanelLED implements AutoCloseable {
    private final EventBus bus;
    private final PrintStream salida;
    private final boolean colores;
    private final Consumer<PedidoRealizadoEvent> pedidos = evento ->
            mostrar("Nuevo pedido recibido en " + evento.mesaId(), "36");
    private final Consumer<BebidaServidaEvent> bebidas = evento ->
            mostrar(evento.bebida() + " servida en " + evento.mesaId(), "34");
    private final Consumer<ComidaPreparadaEvent> comidas = evento ->
            mostrar(evento.plato() + " preparada para " + evento.pedidoId(), "33");
    private final Consumer<BandaTocandoEvent> musica = evento ->
            mostrar(evento.nombreBanda() + " están tocando " + evento.nombreCancion(), "35");

    public PanelLED(EventBus bus) {
        this(bus, false);
    }

    public PanelLED(EventBus bus, boolean colores) {
        this(bus, System.out, colores);
    }

    PanelLED(EventBus bus, PrintStream salida, boolean colores) {
        this.bus = Objects.requireNonNull(bus);
        this.salida = Objects.requireNonNull(salida);
        this.colores = colores;
        bus.suscribir(PedidoRealizadoEvent.class, pedidos);
        bus.suscribir(BebidaServidaEvent.class, bebidas);
        bus.suscribir(ComidaPreparadaEvent.class, comidas);
        bus.suscribir(BandaTocandoEvent.class, musica);
    }

    private void mostrar(String mensaje, String color) {
        String texto = "[PANEL LED] " + mensaje;
        salida.println(colores ? "\u001B[" + color + "m" + texto + "\u001B[0m" : texto);
    }

    @Override
    public void close() {
        bus.desuscribir(PedidoRealizadoEvent.class, pedidos);
        bus.desuscribir(BebidaServidaEvent.class, bebidas);
        bus.desuscribir(ComidaPreparadaEvent.class, comidas);
        bus.desuscribir(BandaTocandoEvent.class, musica);
    }
}

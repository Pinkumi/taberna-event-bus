package com.taverneventvus.componentes;

import com.taverneventvus.bus.EventBus;
import com.taverneventvus.eventos.BandaTocandoEvent;

import java.io.PrintStream;
import java.util.Objects;
import java.util.function.Consumer;

public class SistemaDeSonido implements AutoCloseable {
    private final EventBus bus;
    private final Consumer<BandaTocandoEvent> suscriptor;

    public SistemaDeSonido(EventBus bus) {
        this(bus, System.out);
    }

    SistemaDeSonido(EventBus bus, PrintStream salida) {
        this.bus = Objects.requireNonNull(bus);
        Objects.requireNonNull(salida);
        suscriptor = evento -> salida.println("[SONIDO] Ajustando ecualizador para: " + evento.nombreCancion());
        bus.suscribir(BandaTocandoEvent.class, suscriptor);
    }

    @Override
    public void close() {
        bus.desuscribir(BandaTocandoEvent.class, suscriptor);
    }
}

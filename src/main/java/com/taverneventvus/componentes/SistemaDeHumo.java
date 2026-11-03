package com.taverneventvus.componentes;

import com.taverneventvus.bus.EventBus;
import com.taverneventvus.eventos.BandaTocandoEvent;

import java.io.PrintStream;
import java.util.Objects;
import java.util.function.Consumer;

public class SistemaDeHumo implements AutoCloseable {
    private final EventBus bus;
    private final Consumer<BandaTocandoEvent> suscriptor;

    public SistemaDeHumo(EventBus bus) {
        this(bus, System.out);
    }

    SistemaDeHumo(EventBus bus, PrintStream salida) {
        this.bus = Objects.requireNonNull(bus);
        Objects.requireNonNull(salida);
        suscriptor = evento -> {
            if ("Through the Fire and Flames".equals(evento.nombreCancion())) {
                salida.println("[HUMO] Efectos de humo activados para: " + evento.nombreCancion());
            }
        };
        bus.suscribir(BandaTocandoEvent.class, suscriptor);
    }

    @Override
    public void close() {
        bus.desuscribir(BandaTocandoEvent.class, suscriptor);
    }
}

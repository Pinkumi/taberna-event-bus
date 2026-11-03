package com.taverneventvus.componentes;

import com.taverneventvus.bus.EventBus;
import com.taverneventvus.eventos.BandaTocandoEvent;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Banda implements AutoCloseable {
    private final EventBus bus;
    private final String nombre;
    private final ScheduledExecutorService ejecutor =
            Executors.newSingleThreadScheduledExecutor(tarea -> new Thread(tarea, "banda"));
    private ScheduledFuture<?> interpretacion;

    public Banda(EventBus bus, String nombre) {
        this.bus = Objects.requireNonNull(bus);
        this.nombre = Objects.requireNonNull(nombre);
    }

    public void tocarCancion(String nombreCancion, int duracion) {
        if (ejecutor.isShutdown()) {
            throw new IllegalStateException("La banda está detenida");
        }
        var evento = new BandaTocandoEvent(nombre, nombreCancion, duracion);
        System.out.println("[Banda] " + nombre + " tocando " + nombreCancion + " (" + duracion + " s)");
        bus.publicar(evento);
    }

    public synchronized void iniciar(String nombreCancion, int duracion, Duration intervalo) {
        long milisegundos = intervalo.toMillis();
        if (milisegundos <= 0) {
            throw new IllegalArgumentException("El intervalo debe ser de al menos un milisegundo");
        }
        if (ejecutor.isShutdown() || interpretacion != null) {
            throw new IllegalStateException("La banda ya fue iniciada o detenida");
        }
        interpretacion = ejecutor.scheduleAtFixedRate(() -> {
            try {
                tocarCancion(nombreCancion, duracion);
            } catch (RuntimeException error) {
                System.err.println("[Banda] Error al publicar canción: " + error.getMessage());
            }
        }, 0, milisegundos, TimeUnit.MILLISECONDS);
    }

    public void detener() {
        synchronized (this) {
            if (interpretacion != null) {
                interpretacion.cancel(false);
            }
            ejecutor.shutdown();
        }
        try {
            if (!ejecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                ejecutor.shutdownNow();
            }
        } catch (InterruptedException error) {
            ejecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public boolean estaDetenida() {
        return ejecutor.isTerminated();
    }

    @Override
    public void close() {
        detener();
    }
}

package com.taverneventvus.componentes;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

final class PreparacionProgramada implements AutoCloseable {
    private final ScheduledExecutorService ejecutor;
    private final long demoraMillis;

    PreparacionProgramada(Duration demora, String nombreHilo) {
        demoraMillis = demora.toMillis();
        if (demora.isNegative()) {
            throw new IllegalArgumentException("El tiempo de preparación no puede ser negativo");
        }
        ejecutor = Executors.newSingleThreadScheduledExecutor(tarea -> new Thread(tarea, nombreHilo));
    }

    synchronized void programar(Runnable tarea) {
        if (!ejecutor.isShutdown()) {
            ejecutor.schedule(() -> {
                try {
                    tarea.run();
                } catch (RuntimeException error) {
                    System.err.println("Error durante la preparación: " + error.getMessage());
                }
            }, demoraMillis, TimeUnit.MILLISECONDS);
        }
    }

    boolean estaCerrada() {
        return ejecutor.isTerminated();
    }

    @Override
    public void close() {
        synchronized (this) {
            ejecutor.shutdown();
        }
        try {
            if (!ejecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                ejecutor.shutdownNow();
            }
        } catch (InterruptedException error) {
            ejecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

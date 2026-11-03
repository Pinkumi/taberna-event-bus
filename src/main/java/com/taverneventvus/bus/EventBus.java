package com.taverneventvus.bus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/** Bus síncrono con registro seguro entre hilos; distribuye por tipo exacto. */
public class EventBus {
    private final Map<Class<?>, List<Consumer<?>>> suscriptores = new HashMap<>();

    public synchronized <T> void suscribir(Class<T> tipoDeEvento, Consumer<T> suscriptor) {
        Objects.requireNonNull(tipoDeEvento);
        Objects.requireNonNull(suscriptor);
        suscriptores.computeIfAbsent(tipoDeEvento, tipo -> new ArrayList<>()).add(suscriptor);
    }

    public synchronized <T> void desuscribir(Class<T> tipoDeEvento, Consumer<T> suscriptor) {
        Objects.requireNonNull(tipoDeEvento);
        Objects.requireNonNull(suscriptor);
        List<Consumer<?>> registrados = suscriptores.get(tipoDeEvento);
        if (registrados != null) {
            registrados.remove(suscriptor);
            if (registrados.isEmpty()) {
                suscriptores.remove(tipoDeEvento);
            }
        }
    }

    public <T> void publicar(T evento) {
        Objects.requireNonNull(evento);
        List<Consumer<?>> copia;
        synchronized (this) {
            copia = List.copyOf(suscriptores.getOrDefault(evento.getClass(), List.of()));
        }
        for (Consumer<?> suscriptor : copia) {
            notificar(suscriptor, evento);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void notificar(Consumer<?> suscriptor, T evento) {
        // El registro y la búsqueda por clase garantizan el tipo del suscriptor.
        ((Consumer<T>) suscriptor).accept(evento);
    }
}

package com.taverneventvus.bus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/** Bus síncrono para uso en un solo hilo; distribuye por tipo exacto de evento. */
public class EventBus {
    private final Map<Class<?>, List<Consumer<?>>> suscriptores = new HashMap<>();

    public <T> void suscribir(Class<T> tipoDeEvento, Consumer<T> suscriptor) {
        Objects.requireNonNull(tipoDeEvento);
        Objects.requireNonNull(suscriptor);
        suscriptores.computeIfAbsent(tipoDeEvento, tipo -> new ArrayList<>()).add(suscriptor);
    }

    public <T> void desuscribir(Class<T> tipoDeEvento, Consumer<T> suscriptor) {
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
        List<Consumer<?>> registrados = suscriptores.get(evento.getClass());
        if (registrados != null) {
            for (Consumer<?> suscriptor : List.copyOf(registrados)) {
                notificar(suscriptor, evento);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void notificar(Consumer<?> suscriptor, T evento) {
        // El registro y la búsqueda por clase garantizan el tipo del suscriptor.
        ((Consumer<T>) suscriptor).accept(evento);
    }
}

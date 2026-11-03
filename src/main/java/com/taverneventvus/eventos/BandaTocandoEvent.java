package com.taverneventvus.eventos;

/** La duración se expresa en segundos. */
public record BandaTocandoEvent(String nombreBanda, String nombreCancion, int duracion) {
}

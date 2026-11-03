package com.taverneventvus.componentes;

import java.util.Locale;
import java.util.Set;

public final class CatalogoProductos {
    private static final Set<String> BEBIDAS = Set.of("cerveza", "refresco", "agua");
    private static final Set<String> ALIMENTOS = Set.of("hamburguesa", "pizza", "papas fritas");

    private CatalogoProductos() {
    }

    public static boolean esBebida(String producto) {
        return BEBIDAS.contains(normalizar(producto));
    }

    public static boolean esAlimento(String producto) {
        return ALIMENTOS.contains(normalizar(producto));
    }

    public static boolean esConocido(String producto) {
        return esBebida(producto) || esAlimento(producto);
    }

    private static String normalizar(String producto) {
        return producto.strip().toLowerCase(Locale.ROOT);
    }
}

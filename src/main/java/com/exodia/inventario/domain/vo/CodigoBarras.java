package com.exodia.inventario.domain.vo;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object que representa un codigo de barras de contenedor.
 * Inmutable. No nulo, no vacio, max 100 caracteres, alfanumerico con guiones.
 */
public record CodigoBarras(String valor) {

    private static final int MAX_LONGITUD = 100;
    private static final Pattern PATRON_VALIDO = Pattern.compile("^[A-Za-z0-9\\-_]+$");

    public CodigoBarras {
        Objects.requireNonNull(valor, "El codigo de barras no puede ser nulo");
        if (valor.isBlank()) {
            throw new IllegalArgumentException("El codigo de barras no puede estar vacio");
        }
        if (valor.length() > MAX_LONGITUD) {
            throw new IllegalArgumentException(
                    "El codigo de barras no puede exceder " + MAX_LONGITUD + " caracteres: " + valor.length());
        }
        if (!PATRON_VALIDO.matcher(valor).matches()) {
            throw new IllegalArgumentException(
                    "El codigo de barras contiene caracteres invalidos: " + valor);
        }
    }

    public static CodigoBarras de(String valor) {
        return new CodigoBarras(valor);
    }

    @Override
    public String toString() {
        return valor;
    }
}

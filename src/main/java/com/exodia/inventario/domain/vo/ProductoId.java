package com.exodia.inventario.domain.vo;

import java.util.Objects;

/**
 * Value Object que representa el identificador de un producto (externo al microservicio).
 * Inmutable. No nulo, > 0.
 */
public record ProductoId(Long valor) {

    public ProductoId {
        Objects.requireNonNull(valor, "El ID de producto no puede ser nulo");
        if (valor <= 0) {
            throw new IllegalArgumentException("El ID de producto debe ser positivo: " + valor);
        }
    }

    public static ProductoId de(Long valor) {
        return new ProductoId(valor);
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}

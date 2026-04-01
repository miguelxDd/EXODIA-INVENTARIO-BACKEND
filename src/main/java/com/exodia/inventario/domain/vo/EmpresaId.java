package com.exodia.inventario.domain.vo;

import java.util.Objects;

/**
 * Value Object que representa el identificador de una empresa.
 * Inmutable. No nulo, > 0.
 */
public record EmpresaId(Long valor) {

    public EmpresaId {
        Objects.requireNonNull(valor, "El ID de empresa no puede ser nulo");
        if (valor <= 0) {
            throw new IllegalArgumentException("El ID de empresa debe ser positivo: " + valor);
        }
    }

    public static EmpresaId de(Long valor) {
        return new EmpresaId(valor);
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}

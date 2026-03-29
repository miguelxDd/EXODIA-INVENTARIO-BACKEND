package com.exodia.inventario.domain.vo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value Object que representa un valor monetario.
 * Inmutable. No nulo, >= 0, escala 6.
 */
public record Dinero(BigDecimal valor) {

    private static final int ESCALA = 6;

    public Dinero {
        Objects.requireNonNull(valor, "El valor monetario no puede ser nulo");
        if (valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El valor monetario no puede ser negativo: " + valor);
        }
        valor = valor.setScale(ESCALA, RoundingMode.HALF_UP);
    }

    public static Dinero de(BigDecimal valor) {
        return new Dinero(valor);
    }

    public static Dinero de(String valor) {
        return new Dinero(new BigDecimal(valor));
    }

    public static Dinero cero() {
        return new Dinero(BigDecimal.ZERO);
    }

    public Dinero sumar(Dinero otro) {
        return new Dinero(this.valor.add(otro.valor));
    }

    public Dinero multiplicar(BigDecimal factor) {
        return new Dinero(this.valor.multiply(factor).setScale(ESCALA, RoundingMode.HALF_UP));
    }

    public boolean esCero() {
        return this.valor.compareTo(BigDecimal.ZERO) == 0;
    }

    @Override
    public String toString() {
        return valor.toPlainString();
    }
}

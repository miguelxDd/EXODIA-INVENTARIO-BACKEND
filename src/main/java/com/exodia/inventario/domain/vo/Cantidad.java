package com.exodia.inventario.domain.vo;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object que representa una cantidad de inventario.
 * Inmutable. Siempre >= 0.
 */
public record Cantidad(BigDecimal valor) {

    public Cantidad {
        Objects.requireNonNull(valor, "La cantidad no puede ser nula");
        if (valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa: " + valor);
        }
    }

    public static Cantidad de(BigDecimal valor) {
        return new Cantidad(valor);
    }

    public static Cantidad de(long valor) {
        return new Cantidad(BigDecimal.valueOf(valor));
    }

    public static Cantidad de(String valor) {
        return new Cantidad(new BigDecimal(valor));
    }

    public static Cantidad cero() {
        return new Cantidad(BigDecimal.ZERO);
    }

    public Cantidad sumar(Cantidad otra) {
        return new Cantidad(this.valor.add(otra.valor));
    }

    public Cantidad restar(Cantidad otra) {
        return new Cantidad(this.valor.subtract(otra.valor));
    }

    public boolean esMayorQue(Cantidad otra) {
        return this.valor.compareTo(otra.valor) > 0;
    }

    public boolean esMayorOIgualQue(Cantidad otra) {
        return this.valor.compareTo(otra.valor) >= 0;
    }

    public boolean esMenorQue(Cantidad otra) {
        return this.valor.compareTo(otra.valor) < 0;
    }

    public boolean esCero() {
        return this.valor.compareTo(BigDecimal.ZERO) == 0;
    }

    @Override
    public String toString() {
        return valor.toPlainString();
    }
}

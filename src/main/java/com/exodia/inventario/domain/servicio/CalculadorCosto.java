package com.exodia.inventario.domain.servicio;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Servicio de dominio puro (sin Spring) para calculos de costo de inventario.
 * Implementa costo promedio ponderado.
 */
public class CalculadorCosto {

    private static final int ESCALA = 6;

    /**
     * Calcula el costo promedio ponderado despues de una nueva entrada de inventario.
     *
     * Formula: (stockExistente * costoActual + cantidadNueva * costoNuevo) / (stockExistente + cantidadNueva)
     *
     * @param stockExistente cantidad actual en stock
     * @param costoActual costo unitario promedio actual
     * @param cantidadNueva cantidad que ingresa
     * @param costoNuevo costo unitario de la nueva entrada
     * @return nuevo costo promedio ponderado
     */
    public BigDecimal calcularPromedioPoInterado(BigDecimal stockExistente, BigDecimal costoActual,
                                                  BigDecimal cantidadNueva, BigDecimal costoNuevo) {
        if (stockExistente == null) stockExistente = BigDecimal.ZERO;
        if (costoActual == null) costoActual = BigDecimal.ZERO;
        if (cantidadNueva == null || cantidadNueva.compareTo(BigDecimal.ZERO) <= 0) {
            return costoActual;
        }
        if (costoNuevo == null) costoNuevo = BigDecimal.ZERO;

        BigDecimal valorExistente = stockExistente.multiply(costoActual);
        BigDecimal valorNuevo = cantidadNueva.multiply(costoNuevo);
        BigDecimal totalCantidad = stockExistente.add(cantidadNueva);

        if (totalCantidad.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return valorExistente.add(valorNuevo)
                .divide(totalCantidad, ESCALA, RoundingMode.HALF_UP);
    }

    /**
     * Calcula el valor total de inventario para un contenedor.
     *
     * @param cantidad stock del contenedor
     * @param precioUnitario precio unitario
     * @return valor total = cantidad * precioUnitario
     */
    public BigDecimal calcularValorTotal(BigDecimal cantidad, BigDecimal precioUnitario) {
        if (cantidad == null || precioUnitario == null) {
            return BigDecimal.ZERO;
        }
        return cantidad.multiply(precioUnitario).setScale(ESCALA, RoundingMode.HALF_UP);
    }
}

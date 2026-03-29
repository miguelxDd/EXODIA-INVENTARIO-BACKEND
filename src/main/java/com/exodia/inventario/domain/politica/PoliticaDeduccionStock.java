package com.exodia.inventario.domain.politica;

import com.exodia.inventario.domain.enums.EstadoContenedorCodigo;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Specification/Politica de dominio pura (sin Spring) que valida si se puede
 * deducir stock de un contenedor.
 *
 * Condiciones:
 * 1. El contenedor debe estar en un estado operable.
 * 2. La cantidad a deducir debe ser positiva.
 * 3. El stock disponible debe ser suficiente.
 */
public class PoliticaDeduccionStock {

    private static final Set<String> ESTADOS_OPERABLES = Set.of(
            EstadoContenedorCodigo.DISPONIBLE.getCodigo(),
            EstadoContenedorCodigo.RESERVADO.getCodigo()
    );

    /**
     * Evalua si se puede deducir stock de un contenedor.
     *
     * @param estadoContenedor codigo del estado actual del contenedor
     * @param stockDisponible stock disponible (total - reservado)
     * @param cantidadSolicitada cantidad a deducir
     * @return resultado de la evaluacion con razon si falla
     */
    public ResultadoValidacion evaluar(String estadoContenedor,
                                       BigDecimal stockDisponible,
                                       BigDecimal cantidadSolicitada) {
        if (cantidadSolicitada == null || cantidadSolicitada.compareTo(BigDecimal.ZERO) <= 0) {
            return ResultadoValidacion.fallo("La cantidad a deducir debe ser mayor a cero");
        }

        if (estadoContenedor == null || !ESTADOS_OPERABLES.contains(estadoContenedor)) {
            return ResultadoValidacion.fallo(
                    "El contenedor no esta en estado operable: " + estadoContenedor);
        }

        if (stockDisponible == null || stockDisponible.compareTo(cantidadSolicitada) < 0) {
            return ResultadoValidacion.fallo(String.format(
                    "Stock insuficiente: disponible=%s, solicitada=%s",
                    stockDisponible != null ? stockDisponible.toPlainString() : "0",
                    cantidadSolicitada.toPlainString()));
        }

        return ResultadoValidacion.exitoso();
    }

    /**
     * Resultado de la validacion de deduccion.
     */
    public record ResultadoValidacion(boolean valido, String razon) {

        public static ResultadoValidacion exitoso() {
            return new ResultadoValidacion(true, null);
        }

        public static ResultadoValidacion fallo(String razon) {
            return new ResultadoValidacion(false, razon);
        }
    }
}

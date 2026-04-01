package com.exodia.inventario.domain.politica;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Specification/Politica de dominio pura (sin Spring) que valida si se puede
 * crear una reserva sobre un contenedor.
 *
 * Condiciones:
 * 1. La cantidad a reservar debe ser positiva.
 * 2. El stock disponible (total - ya reservado) debe ser suficiente.
 * 3. La fecha de expiracion (si se proporciona) debe ser futura.
 */
public class PoliticaReserva {

    /**
     * Evalua si se puede crear una reserva.
     *
     * @param stockDisponible stock disponible del contenedor
     * @param cantidadAReservar cantidad que se desea reservar
     * @param fechaExpiracion fecha de expiracion de la reserva (puede ser null)
     * @return resultado de la evaluacion
     */
    public ResultadoValidacion evaluar(BigDecimal stockDisponible,
                                       BigDecimal cantidadAReservar,
                                       OffsetDateTime fechaExpiracion) {
        if (cantidadAReservar == null || cantidadAReservar.compareTo(BigDecimal.ZERO) <= 0) {
            return ResultadoValidacion.fallo("La cantidad a reservar debe ser mayor a cero");
        }

        if (stockDisponible == null || stockDisponible.compareTo(cantidadAReservar) < 0) {
            return ResultadoValidacion.fallo(String.format(
                    "Stock insuficiente para reservar: disponible=%s, solicitada=%s",
                    stockDisponible != null ? stockDisponible.toPlainString() : "0",
                    cantidadAReservar.toPlainString()));
        }

        if (fechaExpiracion != null && fechaExpiracion.isBefore(OffsetDateTime.now())) {
            return ResultadoValidacion.fallo(
                    "La fecha de expiracion de la reserva debe ser futura: " + fechaExpiracion);
        }

        return ResultadoValidacion.exitoso();
    }

    /**
     * Resultado de la validacion de reserva.
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

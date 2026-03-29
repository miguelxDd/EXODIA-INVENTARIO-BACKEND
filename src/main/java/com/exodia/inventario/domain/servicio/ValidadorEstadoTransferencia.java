package com.exodia.inventario.domain.servicio;

import com.exodia.inventario.domain.enums.EstadoTransferenciaCodigo;

import java.util.Map;
import java.util.Set;

/**
 * Servicio de dominio puro (sin Spring) que valida las transiciones de estado
 * de la maquina de estados de transferencias.
 *
 * Maquina de estados:
 * BORRADOR -> CONFIRMADO -> DESPACHADO -> EN_TRANSITO -> RECIBIDO_PARCIAL -> RECIBIDO_COMPLETO
 *                       |                                                           |
 *                       +-> CANCELADO <---------------------------------------------+
 *                                                                     CIERRE_FORZADO
 */
public class ValidadorEstadoTransferencia {

    private static final Map<EstadoTransferenciaCodigo, Set<EstadoTransferenciaCodigo>> TRANSICIONES_VALIDAS =
            Map.of(
                    EstadoTransferenciaCodigo.BORRADOR,
                    Set.of(EstadoTransferenciaCodigo.CONFIRMADO, EstadoTransferenciaCodigo.CANCELADO),

                    EstadoTransferenciaCodigo.CONFIRMADO,
                    Set.of(EstadoTransferenciaCodigo.DESPACHADO, EstadoTransferenciaCodigo.CANCELADO),

                    EstadoTransferenciaCodigo.DESPACHADO,
                    Set.of(EstadoTransferenciaCodigo.EN_TRANSITO),

                    EstadoTransferenciaCodigo.EN_TRANSITO,
                    Set.of(EstadoTransferenciaCodigo.RECIBIDO_PARCIAL,
                            EstadoTransferenciaCodigo.RECIBIDO_COMPLETO),

                    EstadoTransferenciaCodigo.RECIBIDO_PARCIAL,
                    Set.of(EstadoTransferenciaCodigo.RECIBIDO_COMPLETO,
                            EstadoTransferenciaCodigo.CIERRE_FORZADO),

                    EstadoTransferenciaCodigo.RECIBIDO_COMPLETO,
                    Set.of(),

                    EstadoTransferenciaCodigo.CANCELADO,
                    Set.of(),

                    EstadoTransferenciaCodigo.CIERRE_FORZADO,
                    Set.of()
            );

    /**
     * Valida si una transicion de estado es permitida.
     *
     * @param actual estado actual de la transferencia
     * @param nuevo estado al que se desea transicionar
     * @return true si la transicion es valida
     */
    public boolean esTransicionValida(EstadoTransferenciaCodigo actual,
                                       EstadoTransferenciaCodigo nuevo) {
        if (actual == null || nuevo == null) {
            return false;
        }
        Set<EstadoTransferenciaCodigo> permitidos = TRANSICIONES_VALIDAS.get(actual);
        return permitidos != null && permitidos.contains(nuevo);
    }

    /**
     * Valida la transicion y lanza excepcion si es invalida.
     *
     * @param actual estado actual
     * @param nuevo estado deseado
     * @throws IllegalStateException si la transicion no es valida
     */
    public void validarTransicion(EstadoTransferenciaCodigo actual,
                                   EstadoTransferenciaCodigo nuevo) {
        if (!esTransicionValida(actual, nuevo)) {
            throw new IllegalStateException(String.format(
                    "Transicion de estado invalida: %s -> %s", actual, nuevo));
        }
    }

    /**
     * Retorna los estados a los que se puede transicionar desde el estado actual.
     */
    public Set<EstadoTransferenciaCodigo> obtenerTransicionesPermitidas(
            EstadoTransferenciaCodigo actual) {
        if (actual == null) {
            return Set.of();
        }
        return TRANSICIONES_VALIDAS.getOrDefault(actual, Set.of());
    }

    /**
     * Verifica si el estado permite cancelacion.
     */
    public boolean permiteCancelacion(EstadoTransferenciaCodigo actual) {
        return esTransicionValida(actual, EstadoTransferenciaCodigo.CANCELADO);
    }
}

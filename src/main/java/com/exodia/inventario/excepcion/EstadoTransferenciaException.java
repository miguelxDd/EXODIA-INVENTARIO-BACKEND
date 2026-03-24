package com.exodia.inventario.excepcion;

public class EstadoTransferenciaException extends InventarioException {

    public EstadoTransferenciaException(String estadoActual, String estadoSolicitado) {
        super(
            "INV-005",
            400,
            String.format(
                "Transicion de estado invalida: no se puede pasar de '%s' a '%s'",
                estadoActual, estadoSolicitado
            )
        );
    }
}

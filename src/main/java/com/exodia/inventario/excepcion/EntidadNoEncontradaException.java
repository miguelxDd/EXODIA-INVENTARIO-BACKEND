package com.exodia.inventario.excepcion;

public class EntidadNoEncontradaException extends InventarioException {

    public EntidadNoEncontradaException(String entidad, Long id) {
        super(
            "INV-010",
            404,
            String.format("%s con id %d no encontrado", entidad, id)
        );
    }
}

package com.exodia.inventario.excepcion;

public class ConversionNoEncontradaException extends InventarioException {

    public ConversionNoEncontradaException(Long unidadOrigenId, Long unidadDestinoId) {
        super(
            "INV-006",
            404,
            String.format(
                "No se encontro conversion de unidad origen %d a unidad destino %d",
                unidadOrigenId, unidadDestinoId
            )
        );
    }
}

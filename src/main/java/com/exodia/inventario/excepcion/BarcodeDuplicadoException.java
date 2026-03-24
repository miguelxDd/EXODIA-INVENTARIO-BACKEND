package com.exodia.inventario.excepcion;

public class BarcodeDuplicadoException extends InventarioException {

    public BarcodeDuplicadoException(String codigoBarras) {
        super(
            "INV-002",
            409,
            String.format("El codigo de barras '%s' ya existe en el sistema", codigoBarras)
        );
    }
}

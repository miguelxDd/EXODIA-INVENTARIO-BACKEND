package com.exodia.inventario.excepcion;

public class OperacionInvalidaException extends InventarioException {

    public OperacionInvalidaException(String mensaje) {
        super("INV-004", 400, mensaje);
    }
}

package com.exodia.inventario.excepcion;

public class ConflictoReservaException extends InventarioException {

    public ConflictoReservaException(String mensaje) {
        super("INV-007", 409, mensaje);
    }
}

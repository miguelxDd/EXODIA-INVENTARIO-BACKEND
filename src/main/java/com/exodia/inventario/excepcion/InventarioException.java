package com.exodia.inventario.excepcion;

import lombok.Getter;

@Getter
public abstract class InventarioException extends RuntimeException {

    private final String codigoError;
    private final int httpStatus;

    protected InventarioException(String codigoError, int httpStatus, String mensaje) {
        super(mensaje);
        this.codigoError = codigoError;
        this.httpStatus = httpStatus;
    }
}

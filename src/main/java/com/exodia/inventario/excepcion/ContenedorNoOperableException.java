package com.exodia.inventario.excepcion;

public class ContenedorNoOperableException extends InventarioException {

    public ContenedorNoOperableException(Long contenedorId, String estadoActual) {
        super(
            "INV-012",
            409,
            String.format(
                "El contenedor %d no es operable en su estado actual '%s'",
                contenedorId, estadoActual
            )
        );
    }
}

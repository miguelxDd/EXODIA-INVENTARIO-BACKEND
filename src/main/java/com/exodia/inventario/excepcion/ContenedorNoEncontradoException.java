package com.exodia.inventario.excepcion;

public class ContenedorNoEncontradoException extends InventarioException {

    public ContenedorNoEncontradoException(Long id) {
        super(
            "INV-003",
            404,
            String.format("Contenedor con id %d no encontrado", id)
        );
    }

    public ContenedorNoEncontradoException(String codigoBarras) {
        super(
            "INV-003",
            404,
            String.format("Contenedor con codigo de barras '%s' no encontrado", codigoBarras)
        );
    }
}

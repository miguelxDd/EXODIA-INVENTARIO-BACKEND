package com.exodia.inventario.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TipoRecepcion {
    MANUAL("MANUAL"),
    ORDEN_COMPRA("ORDEN_COMPRA"),
    TRANSFERENCIA("TRANSFERENCIA"),
    PRODUCCION("PRODUCCION"),
    DEVOLUCION("DEVOLUCION");

    private final String codigo;
}

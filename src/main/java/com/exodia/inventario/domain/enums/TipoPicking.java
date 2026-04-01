package com.exodia.inventario.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TipoPicking {
    REQUISICION("REQUISICION"),
    ORDEN_VENTA("ORDEN_VENTA"),
    PRODUCCION("PRODUCCION"),
    GENERAL("GENERAL");

    private final String codigo;
}

package com.exodia.inventario.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TipoAjusteCodigo {
    CANTIDAD("CANTIDAD"),
    PRECIO("PRECIO"),
    CANTIDAD_PRECIO("CANTIDAD_PRECIO");

    private final String codigo;
}

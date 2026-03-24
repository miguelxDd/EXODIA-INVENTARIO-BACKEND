package com.exodia.inventario.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TipoMerma {
    MANUAL("MANUAL"),
    AUTOMATICA("AUTOMATICA"),
    POR_PRODUCTO("POR_PRODUCTO");

    private final String codigo;
}

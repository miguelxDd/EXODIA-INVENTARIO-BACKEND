package com.exodia.inventario.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TipoTransferencia {
    POR_CONTENEDOR("POR_CONTENEDOR"),
    POR_PRODUCTO("POR_PRODUCTO");

    private final String codigo;
}

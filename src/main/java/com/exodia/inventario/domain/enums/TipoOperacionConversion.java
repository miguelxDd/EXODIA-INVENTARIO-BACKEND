package com.exodia.inventario.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TipoOperacionConversion {
    MULTIPLICAR("MULTIPLICAR"),
    DIVIDIR("DIVIDIR");

    private final String codigo;
}

package com.exodia.inventario.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EstadoStock {
    DEBAJO("DEBAJO"),
    EN_RANGO("EN_RANGO"),
    ARRIBA("ARRIBA");

    private final String codigo;
}

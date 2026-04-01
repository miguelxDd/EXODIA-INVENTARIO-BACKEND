package com.exodia.inventario.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TipoUbicacion {
    GENERAL("GENERAL"),
    STANDBY("STANDBY"),
    TEMPORAL("TEMPORAL"),
    RECEPCION("RECEPCION"),
    PRODUCCION("PRODUCCION");

    private final String codigo;
}

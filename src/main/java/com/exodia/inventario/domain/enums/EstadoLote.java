package com.exodia.inventario.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EstadoLote {
    ACTIVO("ACTIVO"),
    CUARENTENA("CUARENTENA"),
    VENCIDO("VENCIDO"),
    BLOQUEADO("BLOQUEADO"),
    CONSUMIDO("CONSUMIDO");

    private final String codigo;
}

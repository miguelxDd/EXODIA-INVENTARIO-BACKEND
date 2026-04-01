package com.exodia.inventario.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EstadoContenedorCodigo {
    DISPONIBLE("DISPONIBLE"),
    RESERVADO("RESERVADO"),
    EN_TRANSITO("EN_TRANSITO"),
    EN_STANDBY("EN_STANDBY"),
    CUARENTENA("CUARENTENA"),
    BLOQUEADO("BLOQUEADO"),
    AGOTADO("AGOTADO");

    private final String codigo;
}

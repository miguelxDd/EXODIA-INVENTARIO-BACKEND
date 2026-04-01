package com.exodia.inventario.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EstadoTransferenciaCodigo {
    BORRADOR("BORRADOR"),
    CONFIRMADO("CONFIRMADO"),
    DESPACHADO("DESPACHADO"),
    EN_TRANSITO("EN_TRANSITO"),
    RECIBIDO_PARCIAL("RECIBIDO_PARCIAL"),
    RECIBIDO_COMPLETO("RECIBIDO_COMPLETO"),
    CANCELADO("CANCELADO"),
    CIERRE_FORZADO("CIERRE_FORZADO");

    private final String codigo;
}

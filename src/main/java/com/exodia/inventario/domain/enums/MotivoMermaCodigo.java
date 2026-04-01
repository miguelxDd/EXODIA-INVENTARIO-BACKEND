package com.exodia.inventario.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MotivoMermaCodigo {
    DANO_OPERATIVO("DANO_OPERATIVO"),
    ROTURA("ROTURA"),
    VENCIMIENTO("VENCIMIENTO"),
    RECEPCION("RECEPCION"),
    CALIDAD("CALIDAD"),
    AJUSTE_ADMINISTRATIVO("AJUSTE_ADMINISTRATIVO"),
    OTRO("OTRO");

    private final String codigo;
}

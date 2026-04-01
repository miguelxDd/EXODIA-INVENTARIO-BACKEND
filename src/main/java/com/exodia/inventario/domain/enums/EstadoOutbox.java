package com.exodia.inventario.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EstadoOutbox {
    PENDIENTE("PENDIENTE"),
    PUBLICADO("PUBLICADO"),
    FALLIDO("FALLIDO");

    private final String codigo;
}

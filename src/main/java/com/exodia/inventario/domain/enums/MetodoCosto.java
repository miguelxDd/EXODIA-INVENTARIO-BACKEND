package com.exodia.inventario.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MetodoCosto {
    PROMEDIO_PONDERADO("PROMEDIO_PONDERADO"),
    FIJO("FIJO"),
    FIFO("FIFO"),
    LIFO("LIFO");

    private final String codigo;
}

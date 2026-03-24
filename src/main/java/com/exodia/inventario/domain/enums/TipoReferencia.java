package com.exodia.inventario.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TipoReferencia {
    RECEPCION("RECEPCION"),
    TRANSFERENCIA("TRANSFERENCIA"),
    PICKING("PICKING"),
    AJUSTE("AJUSTE"),
    ORDEN_COMPRA("ORDEN_COMPRA"),
    VENTA("VENTA"),
    ORDEN_PRODUCCION("ORDEN_PRODUCCION"),
    CONTEO_FISICO("CONTEO_FISICO");

    private final String codigo;
}

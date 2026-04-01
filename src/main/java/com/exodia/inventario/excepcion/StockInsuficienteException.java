package com.exodia.inventario.excepcion;

import java.math.BigDecimal;

public class StockInsuficienteException extends InventarioException {

    public StockInsuficienteException(Long contenedorId, BigDecimal solicitada, BigDecimal disponible) {
        super(
            "INV-001",
            422,
            String.format(
                "Stock insuficiente en contenedor %d: solicitada=%s, disponible=%s",
                contenedorId, solicitada.toPlainString(), disponible.toPlainString()
            )
        );
    }
}

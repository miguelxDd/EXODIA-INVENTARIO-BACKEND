package com.exodia.inventario.unit.dominio.politica;

import com.exodia.inventario.domain.politica.PoliticaDeduccionStock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class PoliticaDeduccionStockTest {

    private PoliticaDeduccionStock politica;

    @BeforeEach
    void setUp() {
        politica = new PoliticaDeduccionStock();
    }

    @Test
    void deberiaPermitirDeduccionConStockSuficienteYEstadoDisponible() {
        var resultado = politica.evaluar("DISPONIBLE", new BigDecimal("100"), new BigDecimal("50"));
        assertTrue(resultado.valido());
        assertNull(resultado.razon());
    }

    @Test
    void deberiaPermitirDeduccionConEstadoReservado() {
        var resultado = politica.evaluar("RESERVADO", new BigDecimal("50"), new BigDecimal("50"));
        assertTrue(resultado.valido());
    }

    @Test
    void deberiaRechazarConEstadoNoOperable() {
        var resultado = politica.evaluar("BLOQUEADO", new BigDecimal("100"), new BigDecimal("50"));
        assertFalse(resultado.valido());
        assertNotNull(resultado.razon());
        assertTrue(resultado.razon().contains("no esta en estado operable"));
    }

    @Test
    void deberiaRechazarConStockInsuficiente() {
        var resultado = politica.evaluar("DISPONIBLE", new BigDecimal("10"), new BigDecimal("50"));
        assertFalse(resultado.valido());
        assertTrue(resultado.razon().contains("Stock insuficiente"));
    }

    @Test
    void deberiaRechazarConCantidadCero() {
        var resultado = politica.evaluar("DISPONIBLE", new BigDecimal("100"), BigDecimal.ZERO);
        assertFalse(resultado.valido());
        assertTrue(resultado.razon().contains("mayor a cero"));
    }

    @Test
    void deberiaRechazarConCantidadNegativa() {
        var resultado = politica.evaluar("DISPONIBLE", new BigDecimal("100"), new BigDecimal("-5"));
        assertFalse(resultado.valido());
    }

    @Test
    void deberiaRechazarConCantidadNula() {
        var resultado = politica.evaluar("DISPONIBLE", new BigDecimal("100"), null);
        assertFalse(resultado.valido());
    }

    @Test
    void deberiaRechazarConEstadoNulo() {
        var resultado = politica.evaluar(null, new BigDecimal("100"), new BigDecimal("50"));
        assertFalse(resultado.valido());
    }

    @Test
    void deberiaRechazarConEstadoEnTransito() {
        var resultado = politica.evaluar("EN_TRANSITO", new BigDecimal("100"), new BigDecimal("50"));
        assertFalse(resultado.valido());
    }

    @Test
    void deberiaPermitirDeduccionExactaDeStockDisponible() {
        var resultado = politica.evaluar("DISPONIBLE", new BigDecimal("50"), new BigDecimal("50"));
        assertTrue(resultado.valido());
    }
}

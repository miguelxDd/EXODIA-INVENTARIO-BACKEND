package com.exodia.inventario.unit.dominio.politica;

import com.exodia.inventario.domain.politica.PoliticaReserva;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class PoliticaReservaTest {

    private PoliticaReserva politica;

    @BeforeEach
    void setUp() {
        politica = new PoliticaReserva();
    }

    @Test
    void deberiaPermitirReservaValida() {
        var resultado = politica.evaluar(
                new BigDecimal("100"), new BigDecimal("50"),
                OffsetDateTime.now().plusDays(2));
        assertTrue(resultado.valido());
    }

    @Test
    void deberiaPermitirReservaSinFechaExpiracion() {
        var resultado = politica.evaluar(
                new BigDecimal("100"), new BigDecimal("50"), null);
        assertTrue(resultado.valido());
    }

    @Test
    void deberiaRechazarConStockInsuficiente() {
        var resultado = politica.evaluar(
                new BigDecimal("10"), new BigDecimal("50"), null);
        assertFalse(resultado.valido());
        assertTrue(resultado.razon().contains("Stock insuficiente"));
    }

    @Test
    void deberiaRechazarConCantidadCero() {
        var resultado = politica.evaluar(
                new BigDecimal("100"), BigDecimal.ZERO, null);
        assertFalse(resultado.valido());
    }

    @Test
    void deberiaRechazarConCantidadNegativa() {
        var resultado = politica.evaluar(
                new BigDecimal("100"), new BigDecimal("-5"), null);
        assertFalse(resultado.valido());
    }

    @Test
    void deberiaRechazarConCantidadNula() {
        var resultado = politica.evaluar(
                new BigDecimal("100"), null, null);
        assertFalse(resultado.valido());
    }

    @Test
    void deberiaRechazarConFechaExpiracionPasada() {
        var resultado = politica.evaluar(
                new BigDecimal("100"), new BigDecimal("50"),
                OffsetDateTime.now().minusDays(1));
        assertFalse(resultado.valido());
        assertTrue(resultado.razon().contains("futura"));
    }

    @Test
    void deberiaPermitirReservaExactaDeStockDisponible() {
        var resultado = politica.evaluar(
                new BigDecimal("50"), new BigDecimal("50"), null);
        assertTrue(resultado.valido());
    }
}

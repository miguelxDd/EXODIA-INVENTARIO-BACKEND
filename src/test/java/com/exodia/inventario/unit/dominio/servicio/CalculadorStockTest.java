package com.exodia.inventario.unit.dominio.servicio;

import com.exodia.inventario.domain.servicio.CalculadorStock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class CalculadorStockTest {

    private CalculadorStock calculador;

    @BeforeEach
    void setUp() {
        calculador = new CalculadorStock();
    }

    @Test
    void deberiaCalcularStockDisponible() {
        BigDecimal resultado = calculador.calcularStockDisponible(
                new BigDecimal("100"), new BigDecimal("30"));
        assertEquals(0, new BigDecimal("70").compareTo(resultado));
    }

    @Test
    void deberiaRetornarStockTotalSiNoHayReservas() {
        BigDecimal resultado = calculador.calcularStockDisponible(
                new BigDecimal("50"), BigDecimal.ZERO);
        assertEquals(0, new BigDecimal("50").compareTo(resultado));
    }

    @Test
    void deberiaManejarNulosComoZero() {
        BigDecimal resultado = calculador.calcularStockDisponible(null, null);
        assertEquals(0, BigDecimal.ZERO.compareTo(resultado));
    }

    @Test
    void deberiaDetectarStockSuficiente() {
        assertTrue(calculador.hayStockSuficiente(new BigDecimal("100"), new BigDecimal("50")));
        assertTrue(calculador.hayStockSuficiente(new BigDecimal("50"), new BigDecimal("50")));
    }

    @Test
    void deberiaDetectarStockInsuficiente() {
        assertFalse(calculador.hayStockSuficiente(new BigDecimal("10"), new BigDecimal("50")));
    }

    @Test
    void deberiaDetectarStockInsuficienteConNulos() {
        assertFalse(calculador.hayStockSuficiente(null, new BigDecimal("10")));
        assertFalse(calculador.hayStockSuficiente(new BigDecimal("10"), null));
    }

    @Test
    void deberiaAplicarSignoPositivo() {
        BigDecimal resultado = calculador.aplicarSigno(new BigDecimal("100"), 1);
        assertEquals(0, new BigDecimal("100").compareTo(resultado));
    }

    @Test
    void deberiaAplicarSignoNegativo() {
        BigDecimal resultado = calculador.aplicarSigno(new BigDecimal("100"), -1);
        assertEquals(0, new BigDecimal("-100").compareTo(resultado));
    }

    @Test
    void deberiaAplicarSignoCeroParaInformativo() {
        BigDecimal resultado = calculador.aplicarSigno(new BigDecimal("100"), 0);
        assertEquals(0, BigDecimal.ZERO.compareTo(resultado));
    }

    @Test
    void deberiaFallarConCantidadNulaEnSigno() {
        assertThrows(IllegalArgumentException.class,
                () -> calculador.aplicarSigno(null, 1));
    }
}

package com.exodia.inventario.unit.dominio.servicio;

import com.exodia.inventario.domain.servicio.CalculadorCosto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class CalculadorCostoTest {

    private CalculadorCosto calculador;

    @BeforeEach
    void setUp() {
        calculador = new CalculadorCosto();
    }

    @Test
    void deberiaCalcularPromedioPonderado() {
        // Stock: 100 @ $10. Ingreso: 50 @ $20. Nuevo costo = (100*10 + 50*20) / 150 = 2000/150 = 13.333333
        BigDecimal resultado = calculador.calcularPromedioPoInterado(
                new BigDecimal("100"), new BigDecimal("10"),
                new BigDecimal("50"), new BigDecimal("20"));
        assertEquals(0, new BigDecimal("13.333333").compareTo(resultado));
    }

    @Test
    void deberiaRetornarCostoNuevoSiNoHayStockPrevio() {
        BigDecimal resultado = calculador.calcularPromedioPoInterado(
                BigDecimal.ZERO, BigDecimal.ZERO,
                new BigDecimal("50"), new BigDecimal("15"));
        assertEquals(0, new BigDecimal("15.000000").compareTo(resultado));
    }

    @Test
    void deberiaRetornarCostoActualSiCantidadNuevaEsCero() {
        BigDecimal resultado = calculador.calcularPromedioPoInterado(
                new BigDecimal("100"), new BigDecimal("10"),
                BigDecimal.ZERO, new BigDecimal("20"));
        assertEquals(0, new BigDecimal("10").compareTo(resultado));
    }

    @Test
    void deberiaManejarNulosComoZero() {
        BigDecimal resultado = calculador.calcularPromedioPoInterado(
                null, null, new BigDecimal("10"), new BigDecimal("5"));
        assertEquals(0, new BigDecimal("5.000000").compareTo(resultado));
    }

    @Test
    void deberiaCalcularValorTotal() {
        BigDecimal resultado = calculador.calcularValorTotal(
                new BigDecimal("100"), new BigDecimal("25.50"));
        assertEquals(0, new BigDecimal("2550.000000").compareTo(resultado));
    }

    @Test
    void deberiaRetornarZeroConNulos() {
        assertEquals(0, BigDecimal.ZERO.compareTo(calculador.calcularValorTotal(null, null)));
    }
}

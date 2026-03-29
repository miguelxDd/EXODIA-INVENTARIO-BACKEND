package com.exodia.inventario.unit.dominio.vo;

import com.exodia.inventario.domain.vo.Dinero;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class DineroTest {

    @Test
    void deberiaCrearDineroValido() {
        Dinero dinero = Dinero.de(new BigDecimal("99.99"));
        assertEquals(0, new BigDecimal("99.990000").compareTo(dinero.valor()));
    }

    @Test
    void deberiaAjustarEscalaA6() {
        Dinero dinero = Dinero.de("100");
        assertEquals(6, dinero.valor().scale());
    }

    @Test
    void deberiaCrearDineroCero() {
        Dinero dinero = Dinero.cero();
        assertTrue(dinero.esCero());
    }

    @Test
    void deberiaFallarConValorNulo() {
        assertThrows(NullPointerException.class, () -> Dinero.de((BigDecimal) null));
    }

    @Test
    void deberiaFallarConValorNegativo() {
        assertThrows(IllegalArgumentException.class, () -> Dinero.de("-1"));
    }

    @Test
    void deberiaSumarDinero() {
        Dinero a = Dinero.de("100");
        Dinero b = Dinero.de("50.50");
        Dinero resultado = a.sumar(b);
        assertEquals(0, new BigDecimal("150.500000").compareTo(resultado.valor()));
    }

    @Test
    void deberiaMultiplicarPorFactor() {
        Dinero dinero = Dinero.de("10");
        Dinero resultado = dinero.multiplicar(new BigDecimal("3"));
        assertEquals(0, new BigDecimal("30.000000").compareTo(resultado.valor()));
    }
}

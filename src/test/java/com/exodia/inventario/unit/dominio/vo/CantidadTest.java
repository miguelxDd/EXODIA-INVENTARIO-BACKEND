package com.exodia.inventario.unit.dominio.vo;

import com.exodia.inventario.domain.vo.Cantidad;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class CantidadTest {

    @Test
    void deberiaCrearCantidadValida() {
        Cantidad cantidad = Cantidad.de(new BigDecimal("10.5"));
        assertEquals(new BigDecimal("10.5"), cantidad.valor());
    }

    @Test
    void deberiaCrearCantidadCero() {
        Cantidad cantidad = Cantidad.cero();
        assertTrue(cantidad.esCero());
    }

    @Test
    void deberiaCrearDesdeLong() {
        Cantidad cantidad = Cantidad.de(100);
        assertEquals(BigDecimal.valueOf(100), cantidad.valor());
    }

    @Test
    void deberiaCrearDesdeString() {
        Cantidad cantidad = Cantidad.de("25.123456");
        assertEquals(new BigDecimal("25.123456"), cantidad.valor());
    }

    @Test
    void deberiaFallarConCantidadNula() {
        assertThrows(NullPointerException.class, () -> Cantidad.de((BigDecimal) null));
    }

    @Test
    void deberiaFallarConCantidadNegativa() {
        assertThrows(IllegalArgumentException.class, () -> Cantidad.de(new BigDecimal("-1")));
    }

    @Test
    void deberiaSumarCantidades() {
        Cantidad a = Cantidad.de(10);
        Cantidad b = Cantidad.de(5);
        Cantidad resultado = a.sumar(b);
        assertEquals(BigDecimal.valueOf(15), resultado.valor());
    }

    @Test
    void deberiaRestarCantidades() {
        Cantidad a = Cantidad.de(10);
        Cantidad b = Cantidad.de(3);
        Cantidad resultado = a.restar(b);
        assertEquals(BigDecimal.valueOf(7), resultado.valor());
    }

    @Test
    void deberiaFallarAlRestarMasDeLoDisponible() {
        Cantidad a = Cantidad.de(3);
        Cantidad b = Cantidad.de(10);
        assertThrows(IllegalArgumentException.class, () -> a.restar(b));
    }

    @Test
    void deberiaCompararCorrectamente() {
        Cantidad grande = Cantidad.de(100);
        Cantidad chica = Cantidad.de(10);

        assertTrue(grande.esMayorQue(chica));
        assertTrue(chica.esMenorQue(grande));
        assertFalse(chica.esMayorQue(grande));
        assertTrue(grande.esMayorOIgualQue(Cantidad.de(100)));
    }
}

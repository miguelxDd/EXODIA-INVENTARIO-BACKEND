package com.exodia.inventario.unit.dominio.vo;

import com.exodia.inventario.domain.vo.CodigoBarras;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class CodigoBarrasTest {

    @Test
    void deberiaCrearCodigoBarrasValido() {
        CodigoBarras codigo = CodigoBarras.de("INV-00000001");
        assertEquals("INV-00000001", codigo.valor());
    }

    @Test
    void deberiaFallarConCodigoNulo() {
        assertThrows(NullPointerException.class, () -> CodigoBarras.de(null));
    }

    @Test
    void deberiaFallarConCodigoVacio() {
        assertThrows(IllegalArgumentException.class, () -> CodigoBarras.de(""));
    }

    @Test
    void deberiaFallarConCodigoEnBlanco() {
        assertThrows(IllegalArgumentException.class, () -> CodigoBarras.de("   "));
    }

    @Test
    void deberiaFallarConCaracteresInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> CodigoBarras.de("INV 001"));
        assertThrows(IllegalArgumentException.class, () -> CodigoBarras.de("INV@001"));
    }

    @Test
    void deberiaFallarConCodigoMuyLargo() {
        String largo = "A".repeat(101);
        assertThrows(IllegalArgumentException.class, () -> CodigoBarras.de(largo));
    }

    @Test
    void deberiaAceptarCaracteresPermitidos() {
        assertDoesNotThrow(() -> CodigoBarras.de("INV-001_abc-XYZ_123"));
    }

    @Test
    void deberiaSerIgualConMismoValor() {
        CodigoBarras a = CodigoBarras.de("INV-001");
        CodigoBarras b = CodigoBarras.de("INV-001");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}

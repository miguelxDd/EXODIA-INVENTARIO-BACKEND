package com.exodia.inventario.unit.dominio.vo;

import com.exodia.inventario.domain.vo.EmpresaId;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class EmpresaIdTest {

    @Test
    void deberiaCrearEmpresaIdValido() {
        EmpresaId id = EmpresaId.de(1L);
        assertEquals(1L, id.valor());
    }

    @Test
    void deberiaFallarConIdNulo() {
        assertThrows(NullPointerException.class, () -> EmpresaId.de(null));
    }

    @Test
    void deberiaFallarConIdCero() {
        assertThrows(IllegalArgumentException.class, () -> EmpresaId.de(0L));
    }

    @Test
    void deberiaFallarConIdNegativo() {
        assertThrows(IllegalArgumentException.class, () -> EmpresaId.de(-5L));
    }

    @Test
    void deberiaSerIgualConMismoValor() {
        EmpresaId a = EmpresaId.de(42L);
        EmpresaId b = EmpresaId.de(42L);
        assertEquals(a, b);
    }
}

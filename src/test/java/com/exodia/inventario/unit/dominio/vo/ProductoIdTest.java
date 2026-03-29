package com.exodia.inventario.unit.dominio.vo;

import com.exodia.inventario.domain.vo.ProductoId;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class ProductoIdTest {

    @Test
    void deberiaCrearProductoIdValido() {
        ProductoId id = ProductoId.de(999L);
        assertEquals(999L, id.valor());
    }

    @Test
    void deberiaFallarConIdNulo() {
        assertThrows(NullPointerException.class, () -> ProductoId.de(null));
    }

    @Test
    void deberiaFallarConIdCero() {
        assertThrows(IllegalArgumentException.class, () -> ProductoId.de(0L));
    }

    @Test
    void deberiaFallarConIdNegativo() {
        assertThrows(IllegalArgumentException.class, () -> ProductoId.de(-1L));
    }

    @Test
    void deberiaSerIgualConMismoValor() {
        ProductoId a = ProductoId.de(10L);
        ProductoId b = ProductoId.de(10L);
        assertEquals(a, b);
    }
}

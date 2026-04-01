package com.exodia.inventario.unit.dominio.servicio;

import com.exodia.inventario.domain.servicio.PoliticaFEFO;
import com.exodia.inventario.domain.servicio.PoliticaFEFO.AsignacionContenedor;
import com.exodia.inventario.domain.servicio.PoliticaFEFO.ContenedorConStock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class PoliticaFEFOTest {

    private PoliticaFEFO politica;

    @BeforeEach
    void setUp() {
        politica = new PoliticaFEFO();
    }

    @Test
    void deberiaOrdenarContenedoresPorFechaVencimiento() {
        OffsetDateTime ahora = OffsetDateTime.now();
        List<ContenedorConStock> contenedores = List.of(
                new ContenedorConStock(1L, LocalDate.of(2026, 12, 31), ahora, BigDecimal.TEN),
                new ContenedorConStock(2L, LocalDate.of(2026, 6, 15), ahora, BigDecimal.TEN),
                new ContenedorConStock(3L, LocalDate.of(2026, 3, 1), ahora, BigDecimal.TEN)
        );

        List<Long> resultado = politica.ordenarContenedoresParaPicking(contenedores);

        assertEquals(List.of(3L, 2L, 1L), resultado);
    }

    @Test
    void deberiaPonerNullsAlFinalEnFechaVencimiento() {
        OffsetDateTime ahora = OffsetDateTime.now();
        List<ContenedorConStock> contenedores = List.of(
                new ContenedorConStock(1L, null, ahora, BigDecimal.TEN),
                new ContenedorConStock(2L, LocalDate.of(2026, 6, 15), ahora, BigDecimal.TEN)
        );

        List<Long> resultado = politica.ordenarContenedoresParaPicking(contenedores);

        assertEquals(List.of(2L, 1L), resultado);
    }

    @Test
    void deberiaDesempatarPorFechaCreacion() {
        LocalDate mismaFecha = LocalDate.of(2026, 6, 15);
        List<ContenedorConStock> contenedores = List.of(
                new ContenedorConStock(1L, mismaFecha, OffsetDateTime.now().plusHours(1), BigDecimal.TEN),
                new ContenedorConStock(2L, mismaFecha, OffsetDateTime.now(), BigDecimal.TEN)
        );

        List<Long> resultado = politica.ordenarContenedoresParaPicking(contenedores);

        assertEquals(2L, resultado.get(0));
        assertEquals(1L, resultado.get(1));
    }

    @Test
    void deberiaRetornarListaVaciaConEntradaVacia() {
        assertEquals(List.of(), politica.ordenarContenedoresParaPicking(List.of()));
        assertEquals(List.of(), politica.ordenarContenedoresParaPicking(null));
    }

    @Test
    void deberiaSeleccionarContenedoresParaCubrirCantidad() {
        OffsetDateTime ahora = OffsetDateTime.now();
        List<ContenedorConStock> contenedores = List.of(
                new ContenedorConStock(1L, LocalDate.of(2026, 6, 1), ahora, new BigDecimal("30")),
                new ContenedorConStock(2L, LocalDate.of(2026, 9, 1), ahora, new BigDecimal("50")),
                new ContenedorConStock(3L, LocalDate.of(2026, 12, 1), ahora, new BigDecimal("20"))
        );

        List<AsignacionContenedor> resultado = politica.seleccionarContenedores(
                contenedores, new BigDecimal("60"));

        assertEquals(2, resultado.size());
        assertEquals(1L, resultado.get(0).contenedorId());
        assertEquals(0, new BigDecimal("30").compareTo(resultado.get(0).cantidad()));
        assertEquals(2L, resultado.get(1).contenedorId());
        assertEquals(0, new BigDecimal("30").compareTo(resultado.get(1).cantidad()));
    }

    @Test
    void deberiaRetornarTodosLoContenedoresSiNoAlcanza() {
        OffsetDateTime ahora = OffsetDateTime.now();
        List<ContenedorConStock> contenedores = List.of(
                new ContenedorConStock(1L, LocalDate.of(2026, 6, 1), ahora, new BigDecimal("10"))
        );

        List<AsignacionContenedor> resultado = politica.seleccionarContenedores(
                contenedores, new BigDecimal("100"));

        assertEquals(1, resultado.size());
        assertEquals(0, new BigDecimal("10").compareTo(resultado.get(0).cantidad()));
    }

    @Test
    void deberiaIgnorarContenedoresSinStock() {
        OffsetDateTime ahora = OffsetDateTime.now();
        List<ContenedorConStock> contenedores = List.of(
                new ContenedorConStock(1L, LocalDate.of(2026, 6, 1), ahora, BigDecimal.ZERO),
                new ContenedorConStock(2L, LocalDate.of(2026, 9, 1), ahora, new BigDecimal("50"))
        );

        List<AsignacionContenedor> resultado = politica.seleccionarContenedores(
                contenedores, new BigDecimal("30"));

        assertEquals(1, resultado.size());
        assertEquals(2L, resultado.get(0).contenedorId());
    }
}

package com.exodia.inventario.unit.aplicacion.consulta;

import com.exodia.inventario.aplicacion.consulta.StockQueryService;
import com.exodia.inventario.aplicacion.consulta.impl.ReporteInventarioServiceImpl;
import com.exodia.inventario.domain.modelo.catalogo.Bodega;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;
import com.exodia.inventario.domain.modelo.catalogo.EstadoContenedor;
import com.exodia.inventario.domain.modelo.catalogo.TipoOperacion;
import com.exodia.inventario.domain.modelo.catalogo.Ubicacion;
import com.exodia.inventario.domain.modelo.catalogo.Unidad;
import com.exodia.inventario.domain.modelo.contenedor.Contenedor;
import com.exodia.inventario.domain.modelo.contenedor.Operacion;
import com.exodia.inventario.interfaz.dto.respuesta.AuxiliarInventarioResponse;
import com.exodia.inventario.interfaz.dto.respuesta.ValorizacionActualResponse;
import com.exodia.inventario.repositorio.contenedor.OperacionRepository;
import com.exodia.inventario.repositorio.proyeccion.ProductoBodegaStockProjection;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class ReporteInventarioServiceTest {

    @Mock private OperacionRepository operacionRepository;
    @Mock private StockQueryService stockQueryService;

    @InjectMocks
    private ReporteInventarioServiceImpl reporteInventarioService;

    @Test
    void deberiaGenerarAuxiliarInventarioConSaldosAcumulados() {
        OffsetDateTime fechaDesde = OffsetDateTime.parse("2026-03-01T00:00:00Z");
        OffsetDateTime fechaHasta = OffsetDateTime.parse("2026-03-31T23:59:59Z");

        when(operacionRepository.obtenerAcumuladoCantidadAntesDe(1L, 100L, 10L, fechaDesde))
                .thenReturn(new BigDecimal("5"));
        when(operacionRepository.obtenerAcumuladoValorAntesDe(1L, 100L, 10L, fechaDesde))
                .thenReturn(new BigDecimal("25"));
        when(operacionRepository.findAuxiliarInventario(1L, 100L, 10L, fechaDesde, fechaHasta))
                .thenReturn(List.of(
                        crearOperacion(1L, new BigDecimal("3"), new BigDecimal("5"),
                                "RECEPCION", OffsetDateTime.parse("2026-03-05T10:00:00Z")),
                        crearOperacion(2L, new BigDecimal("-2"), new BigDecimal("5"),
                                "PICKING", OffsetDateTime.parse("2026-03-07T10:00:00Z"))));

        AuxiliarInventarioResponse response = reporteInventarioService.generarAuxiliarInventario(
                1L, 100L, 10L, fechaDesde, fechaHasta);

        assertNotNull(response);
        assertEquals(new BigDecimal("5"), response.saldoInicialCantidad());
        assertEquals(new BigDecimal("25"), response.saldoInicialValor());
        assertEquals(new BigDecimal("3"), response.totalEntradas());
        assertEquals(new BigDecimal("2"), response.totalSalidas());
        assertEquals(new BigDecimal("6"), response.saldoFinalCantidad());
        assertEquals(new BigDecimal("30"), response.saldoFinalValor());
        assertEquals(2, response.movimientos().size());
        assertEquals(new BigDecimal("8"), response.movimientos().get(0).saldoCantidad());
        assertEquals(new BigDecimal("40"), response.movimientos().get(0).saldoValor());
    }

    @Test
    void deberiaConstruirValorizacionActual() {
        ProductoBodegaStockProjection projection = new ProductoBodegaStockProjection() {
            @Override
            public Long getProductoId() {
                return 100L;
            }

            @Override
            public Long getBodegaId() {
                return 10L;
            }

            @Override
            public Long getUnidadId() {
                return 1L;
            }

            @Override
            public BigDecimal getStockCantidad() {
                return new BigDecimal("12");
            }
        };

        when(stockQueryService.obtenerStockPorProductoBodega(1L, 10L, 100L))
                .thenReturn(List.of(projection));
        when(stockQueryService.obtenerCostoPromedioPonderado(1L, 100L, 10L))
                .thenReturn(new BigDecimal("4.50"));

        List<ValorizacionActualResponse> response = reporteInventarioService
                .obtenerValorizacionActual(1L, 10L, 100L);

        assertEquals(1, response.size());
        assertEquals(new BigDecimal("12"), response.get(0).cantidadStock());
        assertEquals(new BigDecimal("4.50"), response.get(0).costoUnitario());
        assertEquals(new BigDecimal("54.00"), response.get(0).costoTotal());
    }

    private Operacion crearOperacion(Long id,
                                     BigDecimal cantidad,
                                     BigDecimal precioUnitario,
                                     String tipoOperacionCodigo,
                                     OffsetDateTime fechaOperacion) {
        Empresa empresa = Empresa.builder().codigo("EMP1").nombre("Empresa").build();
        empresa.setId(1L);

        Bodega bodega = Bodega.builder().empresa(empresa).codigo("BOD1").nombre("Bodega").build();
        bodega.setId(10L);

        Unidad unidad = Unidad.builder().empresa(empresa).nombre("Unidad").abreviatura("UND").build();
        unidad.setId(1L);

        Ubicacion ubicacion = Ubicacion.builder().bodega(bodega).codigo("A-01").nombre("UBI1").build();
        ubicacion.setId(1L);

        EstadoContenedor estado = EstadoContenedor.builder()
                .codigo("DISPONIBLE")
                .nombre("Disponible")
                .build();
        estado.setId(1L);

        Contenedor contenedor = Contenedor.builder()
                .empresa(empresa)
                .bodega(bodega)
                .ubicacion(ubicacion)
                .unidad(unidad)
                .productoId(100L)
                .codigoBarras("INV0001")
                .precioUnitario(precioUnitario)
                .estado(estado)
                .build();
        contenedor.setId(1000L);

        TipoOperacion tipoOperacion = TipoOperacion.builder()
                .codigo(tipoOperacionCodigo)
                .nombre(tipoOperacionCodigo)
                .signo((short) cantidad.signum())
                .build();

        Operacion operacion = Operacion.builder()
                .empresa(empresa)
                .contenedor(contenedor)
                .codigoBarras(contenedor.getCodigoBarras())
                .productoId(100L)
                .bodega(bodega)
                .ubicacion(ubicacion)
                .unidad(unidad)
                .tipoOperacion(tipoOperacion)
                .cantidad(cantidad)
                .precioUnitario(precioUnitario)
                .fechaOperacion(fechaOperacion)
                .comentarios("Prueba")
                .build();
        operacion.setId(id);
        return operacion;
    }
}

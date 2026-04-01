package com.exodia.inventario.unit.aplicacion.consulta;

import com.exodia.inventario.aplicacion.comando.ConfiguracionEmpresaService;
import com.exodia.inventario.aplicacion.consulta.impl.StockQueryServiceImpl;
import com.exodia.inventario.domain.modelo.extension.ConfiguracionEmpresa;
import com.exodia.inventario.domain.servicio.CalculadorStock;
import com.exodia.inventario.repositorio.contenedor.ContenedorRepository;
import com.exodia.inventario.repositorio.contenedor.OperacionRepository;
import com.exodia.inventario.repositorio.contenedor.ReservaRepository;
import com.exodia.inventario.repositorio.proyeccion.ContenedorStockProjection;
import com.exodia.inventario.repositorio.proyeccion.ProductoBodegaStockProjection;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class StockQueryServiceTest {

    @Mock
    private OperacionRepository operacionRepository;

    @Mock
    private ContenedorRepository contenedorRepository;

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private CalculadorStock calculadorStock;

    @Mock
    private ConfiguracionEmpresaService configuracionEmpresaService;

    @InjectMocks
    private StockQueryServiceImpl stockQueryService;

    @Test
    void deberiaObtenerStockPorContenedor() {
        when(operacionRepository.obtenerStockPorContenedor(1L))
                .thenReturn(new BigDecimal("100"));

        BigDecimal stock = stockQueryService.obtenerStockContenedor(1L);

        assertEquals(0, new BigDecimal("100").compareTo(stock));
    }

    @Test
    void deberiaObtenerStockPorBarcode() {
        when(operacionRepository.obtenerStockPorBarcode(1L, "INV00000001"))
                .thenReturn(new BigDecimal("50"));

        BigDecimal stock = stockQueryService.obtenerStockPorBarcode(1L, "INV00000001");

        assertEquals(0, new BigDecimal("50").compareTo(stock));
    }

    @Test
    void deberiaObtenerStockPorProductoYBodega() {
        when(operacionRepository.obtenerStockPorProductoYBodega(1L, 100L, 1L))
                .thenReturn(new BigDecimal("200"));

        BigDecimal stock = stockQueryService.obtenerStockPorProductoYBodega(1L, 100L, 1L);

        assertEquals(0, new BigDecimal("200").compareTo(stock));
    }

    @Test
    void deberiaObtenerStockConsolidadoPaginado() {
        Pageable pageable = PageRequest.of(0, 20);
        ContenedorStockProjection mockProjection = mock(ContenedorStockProjection.class);
        Page<ContenedorStockProjection> mockPage = new PageImpl<>(List.of(mockProjection));

        when(operacionRepository.findConsolidatedStock(1L, null, null, null, null, null, pageable))
                .thenReturn(mockPage);

        Page<ContenedorStockProjection> resultado = stockQueryService.obtenerStockConsolidado(
                1L, null, null, null, null, null, pageable);

        assertEquals(1, resultado.getTotalElements());
    }

    @Test
    void deberiaObtenerStockPorProductoBodega() {
        ProductoBodegaStockProjection mockProjection = mock(ProductoBodegaStockProjection.class);

        when(operacionRepository.findStockPorProductoYBodega(1L, null, null))
                .thenReturn(List.of(mockProjection));

        List<ProductoBodegaStockProjection> resultado = stockQueryService
                .obtenerStockPorProductoBodega(1L, null, null);

        assertEquals(1, resultado.size());
    }

    @Test
    void deberiaObtenerContenedoresFEFO() {
        ContenedorStockProjection mockProjection = mock(ContenedorStockProjection.class);

        when(operacionRepository.findContenedoresDisponiblesFEFO(1L, 100L, 1L))
                .thenReturn(List.of(mockProjection));

        List<ContenedorStockProjection> resultado = stockQueryService
                .obtenerContenedoresDisponiblesFEFO(1L, 100L, 1L);

        assertEquals(1, resultado.size());
    }

    @Test
    void deberiaObtenerContenedoresProximosAVencerSegunConfiguracionEmpresa() {
        ContenedorStockProjection mockProjection = mock(ContenedorStockProjection.class);
        ConfiguracionEmpresa config = ConfiguracionEmpresa.builder()
                .diasAlertaVencimiento(15)
                .build();

        when(configuracionEmpresaService.obtenerEntidadOCrear(1L)).thenReturn(config);
        when(operacionRepository.findContenedoresProximosAVencer(1L, 2L, 15))
                .thenReturn(List.of(mockProjection));

        List<ContenedorStockProjection> resultado = stockQueryService
                .obtenerContenedoresProximosAVencer(1L, 2L);

        assertEquals(1, resultado.size());
        verify(operacionRepository).findContenedoresProximosAVencer(1L, 2L, 15);
    }

    @Test
    void deberiaObtenerCantidadReservada() {
        when(reservaRepository.obtenerCantidadReservada(1L))
                .thenReturn(new BigDecimal("30"));

        BigDecimal reservada = stockQueryService.obtenerCantidadReservada(1L);

        assertEquals(0, new BigDecimal("30").compareTo(reservada));
    }

    @Test
    void deberiaCalcularStockDisponible() {
        when(operacionRepository.obtenerStockPorContenedor(1L))
                .thenReturn(new BigDecimal("100"));
        when(reservaRepository.obtenerCantidadReservada(1L))
                .thenReturn(new BigDecimal("30"));
        when(calculadorStock.calcularStockDisponible(new BigDecimal("100"), new BigDecimal("30")))
                .thenReturn(new BigDecimal("70"));

        BigDecimal disponible = stockQueryService.obtenerStockDisponible(1L);

        assertEquals(0, new BigDecimal("70").compareTo(disponible));
        verify(calculadorStock).calcularStockDisponible(new BigDecimal("100"), new BigDecimal("30"));
    }
}

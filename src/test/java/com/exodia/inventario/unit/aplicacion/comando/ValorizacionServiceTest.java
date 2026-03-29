package com.exodia.inventario.unit.aplicacion.comando;

import com.exodia.inventario.aplicacion.comando.impl.ValorizacionServiceImpl;
import com.exodia.inventario.aplicacion.consulta.StockQueryService;
import com.exodia.inventario.domain.modelo.catalogo.Bodega;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;
import com.exodia.inventario.domain.modelo.catalogo.Unidad;
import com.exodia.inventario.domain.modelo.extension.FotoCosto;
import com.exodia.inventario.domain.servicio.CalculadorCosto;
import com.exodia.inventario.excepcion.EntidadNoEncontradaException;
import com.exodia.inventario.interfaz.dto.respuesta.FotoCostoResponse;
import com.exodia.inventario.interfaz.mapeador.FotoCostoMapeador;
import com.exodia.inventario.repositorio.catalogo.BodegaRepository;
import com.exodia.inventario.repositorio.catalogo.EmpresaRepository;
import com.exodia.inventario.repositorio.catalogo.UnidadRepository;
import com.exodia.inventario.repositorio.extension.FotoCostoRepository;
import com.exodia.inventario.repositorio.proyeccion.ProductoBodegaStockProjection;
import org.junit.jupiter.api.BeforeEach;
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
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class ValorizacionServiceTest {

    @Mock private FotoCostoRepository fotoCostoRepository;
    @Mock private EmpresaRepository empresaRepository;
    @Mock private BodegaRepository bodegaRepository;
    @Mock private UnidadRepository unidadRepository;
    @Mock private StockQueryService stockQueryService;
    @Mock private CalculadorCosto calculadorCosto;
    @Mock private FotoCostoMapeador fotoCostoMapeador;

    @InjectMocks
    private ValorizacionServiceImpl valorizacionService;

    private Empresa empresa;
    private Bodega bodega;
    private Unidad unidad;

    @BeforeEach
    void setUp() {
        empresa = Empresa.builder().codigo("EMP1").nombre("Test").build();
        empresa.setId(1L);

        bodega = Bodega.builder().empresa(empresa).codigo("BOD1").nombre("Bodega").build();
        bodega.setId(1L);

        unidad = Unidad.builder().empresa(empresa).codigo("KG").nombre("Kilogramo").abreviatura("kg").build();
        unidad.setId(1L);
    }

    @Test
    void deberiaGenerarFotoCostoExitosamente() {
        ProductoBodegaStockProjection stock = mock(ProductoBodegaStockProjection.class);
        when(stock.getProductoId()).thenReturn(100L);
        when(stock.getBodegaId()).thenReturn(1L);
        when(stock.getUnidadId()).thenReturn(1L);
        when(stock.getStockCantidad()).thenReturn(new BigDecimal("50"));

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(stockQueryService.obtenerStockPorProductoBodega(eq(1L), any(), any()))
                .thenReturn(List.of(stock));
        when(bodegaRepository.findById(1L)).thenReturn(Optional.of(bodega));
        when(unidadRepository.findById(1L)).thenReturn(Optional.of(unidad));
        when(calculadorCosto.calcularValorTotal(any(), any())).thenReturn(BigDecimal.ZERO);
        when(fotoCostoRepository.save(any(FotoCosto.class))).thenAnswer(inv -> {
            FotoCosto f = inv.getArgument(0);
            f.setId(1L);
            return f;
        });

        valorizacionService.generarFotoCosto(1L);

        verify(fotoCostoRepository).save(any(FotoCosto.class));
    }

    @Test
    void deberiaOmitirStockCeroAlGenerarFoto() {
        ProductoBodegaStockProjection stockCero = mock(ProductoBodegaStockProjection.class);
        when(stockCero.getStockCantidad()).thenReturn(BigDecimal.ZERO);

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(stockQueryService.obtenerStockPorProductoBodega(eq(1L), any(), any()))
                .thenReturn(List.of(stockCero));

        valorizacionService.generarFotoCosto(1L);

        verify(fotoCostoRepository, never()).save(any());
    }

    @Test
    void deberiaOmitirStockNegativoAlGenerarFoto() {
        ProductoBodegaStockProjection stockNeg = mock(ProductoBodegaStockProjection.class);
        when(stockNeg.getStockCantidad()).thenReturn(new BigDecimal("-5"));

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(stockQueryService.obtenerStockPorProductoBodega(eq(1L), any(), any()))
                .thenReturn(List.of(stockNeg));

        valorizacionService.generarFotoCosto(1L);

        verify(fotoCostoRepository, never()).save(any());
    }

    @Test
    void deberiaFallarSiEmpresaNoExisteAlGenerarFoto() {
        when(empresaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntidadNoEncontradaException.class,
                () -> valorizacionService.generarFotoCosto(999L));
    }

    @Test
    void deberiaListarFotosCostoPaginadas() {
        FotoCosto foto = FotoCosto.builder().empresa(empresa).productoId(100L).bodega(bodega).build();
        foto.setId(1L);
        Pageable pageable = PageRequest.of(0, 10);
        Page<FotoCosto> page = new PageImpl<>(List.of(foto), pageable, 1);

        when(fotoCostoRepository.findByEmpresaIdOrderByFechaFotoDesc(1L, pageable)).thenReturn(page);
        when(fotoCostoMapeador.toResponse(any(FotoCosto.class)))
                .thenReturn(new FotoCostoResponse(1L, 100L, 1L, 1L,
                        new BigDecimal("50"), BigDecimal.ZERO, BigDecimal.ZERO,
                        "PROMEDIO_PONDERADO", OffsetDateTime.now()));

        Page<FotoCostoResponse> response = valorizacionService.listarFotosCosto(1L, pageable);

        assertEquals(1, response.getTotalElements());
    }
}

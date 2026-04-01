package com.exodia.inventario.unit.aplicacion.comando;

import com.exodia.inventario.aplicacion.comando.impl.MaximoMinimoServiceImpl;
import com.exodia.inventario.domain.modelo.catalogo.Bodega;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;
import com.exodia.inventario.domain.modelo.catalogo.Unidad;
import com.exodia.inventario.domain.modelo.extension.MaximoMinimo;
import com.exodia.inventario.excepcion.EntidadNoEncontradaException;
import com.exodia.inventario.excepcion.OperacionInvalidaException;
import com.exodia.inventario.interfaz.dto.peticion.ActualizarMaximoMinimoRequest;
import com.exodia.inventario.interfaz.dto.peticion.CrearMaximoMinimoRequest;
import com.exodia.inventario.interfaz.dto.respuesta.MaximoMinimoResponse;
import com.exodia.inventario.interfaz.mapeador.MaximoMinimoMapeador;
import com.exodia.inventario.repositorio.catalogo.BodegaRepository;
import com.exodia.inventario.repositorio.catalogo.EmpresaRepository;
import com.exodia.inventario.repositorio.catalogo.UnidadRepository;
import com.exodia.inventario.repositorio.extension.MaximoMinimoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class MaximoMinimoServiceTest {

    @Mock private MaximoMinimoRepository maximoMinimoRepository;
    @Mock private EmpresaRepository empresaRepository;
    @Mock private BodegaRepository bodegaRepository;
    @Mock private UnidadRepository unidadRepository;
    @Mock private MaximoMinimoMapeador maximoMinimoMapeador;

    @InjectMocks
    private MaximoMinimoServiceImpl maximoMinimoService;

    private Empresa empresa;
    private Bodega bodega;
    private Unidad unidad;

    @BeforeEach
    void setUp() {
        empresa = Empresa.builder().codigo("EMP1").nombre("Test").build();
        empresa.setId(1L);

        bodega = Bodega.builder().empresa(empresa).codigo("BOD1").nombre("Bodega").build();
        bodega.setId(1L);
        bodega.setActivo(true);

        unidad = Unidad.builder().empresa(empresa).codigo("KG").nombre("Kilogramo").abreviatura("kg").build();
        unidad.setId(1L);
        unidad.setActivo(true);
    }

    @Test
    void deberiaCrearMaximoMinimoExitosamente() {
        CrearMaximoMinimoRequest request = new CrearMaximoMinimoRequest(
                100L, 1L, 1L, new BigDecimal("10"), new BigDecimal("100"), new BigDecimal("30"));

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(bodegaRepository.findById(1L)).thenReturn(Optional.of(bodega));
        when(unidadRepository.findById(1L)).thenReturn(Optional.of(unidad));
        when(maximoMinimoRepository.findByEmpresaIdAndProductoIdAndBodegaId(1L, 100L, 1L))
                .thenReturn(Optional.empty());
        when(maximoMinimoRepository.save(any(MaximoMinimo.class))).thenAnswer(inv -> {
            MaximoMinimo m = inv.getArgument(0);
            m.setId(1L);
            return m;
        });
        when(maximoMinimoMapeador.toResponse(any(MaximoMinimo.class)))
                .thenReturn(new MaximoMinimoResponse(1L, 100L, 1L, 1L,
                        new BigDecimal("10"), new BigDecimal("100"), new BigDecimal("30"),
                        null, null, true));

        MaximoMinimoResponse response = maximoMinimoService.crear(1L, request);

        assertNotNull(response);
        assertEquals(new BigDecimal("10"), response.stockMinimo());
        assertEquals(new BigDecimal("100"), response.stockMaximo());
    }

    @Test
    void deberiaFallarSiEmpresaNoExiste() {
        when(empresaRepository.findById(999L)).thenReturn(Optional.empty());

        CrearMaximoMinimoRequest request = new CrearMaximoMinimoRequest(
                100L, 1L, 1L, new BigDecimal("10"), new BigDecimal("100"), null);

        assertThrows(EntidadNoEncontradaException.class,
                () -> maximoMinimoService.crear(999L, request));
    }

    @Test
    void deberiaFallarSiYaExisteConfigParaProductoYBodega() {
        MaximoMinimo existente = MaximoMinimo.builder()
                .empresa(empresa).productoId(100L).bodega(bodega).unidad(unidad)
                .stockMinimo(new BigDecimal("5")).stockMaximo(new BigDecimal("50"))
                .build();
        existente.setId(1L);
        existente.setActivo(true);

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(bodegaRepository.findById(1L)).thenReturn(Optional.of(bodega));
        when(unidadRepository.findById(1L)).thenReturn(Optional.of(unidad));
        when(maximoMinimoRepository.findByEmpresaIdAndProductoIdAndBodegaId(1L, 100L, 1L))
                .thenReturn(Optional.of(existente));

        CrearMaximoMinimoRequest request = new CrearMaximoMinimoRequest(
                100L, 1L, 1L, new BigDecimal("10"), new BigDecimal("100"), null);

        assertThrows(OperacionInvalidaException.class,
                () -> maximoMinimoService.crear(1L, request));
    }

    @Test
    void deberiaFallarSiMinimoMayorQueMaximo() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(bodegaRepository.findById(1L)).thenReturn(Optional.of(bodega));
        when(unidadRepository.findById(1L)).thenReturn(Optional.of(unidad));
        when(maximoMinimoRepository.findByEmpresaIdAndProductoIdAndBodegaId(1L, 100L, 1L))
                .thenReturn(Optional.empty());

        CrearMaximoMinimoRequest request = new CrearMaximoMinimoRequest(
                100L, 1L, 1L, new BigDecimal("200"), new BigDecimal("100"), null);

        assertThrows(OperacionInvalidaException.class,
                () -> maximoMinimoService.crear(1L, request));
    }

    @Test
    void deberiaActualizarMaximoMinimoSelectivamente() {
        MaximoMinimo maxMin = MaximoMinimo.builder()
                .empresa(empresa).productoId(100L).bodega(bodega).unidad(unidad)
                .stockMinimo(new BigDecimal("10")).stockMaximo(new BigDecimal("100"))
                .build();
        maxMin.setId(1L);
        maxMin.setActivo(true);

        when(maximoMinimoRepository.findById(1L)).thenReturn(Optional.of(maxMin));
        when(maximoMinimoRepository.save(any(MaximoMinimo.class))).thenReturn(maxMin);
        when(maximoMinimoMapeador.toResponse(any(MaximoMinimo.class)))
                .thenReturn(new MaximoMinimoResponse(1L, 100L, 1L, 1L,
                        new BigDecimal("20"), new BigDecimal("100"), null,
                        null, null, true));

        ActualizarMaximoMinimoRequest request = new ActualizarMaximoMinimoRequest(
                new BigDecimal("20"), null, null);

        MaximoMinimoResponse response = maximoMinimoService.actualizar(1L, 1L, request);

        assertNotNull(response);
        verify(maximoMinimoRepository).save(any(MaximoMinimo.class));
    }

    @Test
    void deberiaFallarAlActualizarSiMinimoSuperaMaximo() {
        MaximoMinimo maxMin = MaximoMinimo.builder()
                .empresa(empresa).productoId(100L).bodega(bodega).unidad(unidad)
                .stockMinimo(new BigDecimal("10")).stockMaximo(new BigDecimal("100"))
                .build();
        maxMin.setId(1L);
        maxMin.setActivo(true);

        when(maximoMinimoRepository.findById(1L)).thenReturn(Optional.of(maxMin));

        ActualizarMaximoMinimoRequest request = new ActualizarMaximoMinimoRequest(
                new BigDecimal("500"), null, null);

        assertThrows(OperacionInvalidaException.class,
                () -> maximoMinimoService.actualizar(1L, 1L, request));
    }

    @Test
    void deberiaDesactivarMaximoMinimo() {
        MaximoMinimo maxMin = MaximoMinimo.builder()
                .empresa(empresa).productoId(100L).bodega(bodega).unidad(unidad)
                .stockMinimo(new BigDecimal("10")).stockMaximo(new BigDecimal("100"))
                .build();
        maxMin.setId(1L);
        maxMin.setActivo(true);

        when(maximoMinimoRepository.findById(1L)).thenReturn(Optional.of(maxMin));
        when(maximoMinimoRepository.save(any(MaximoMinimo.class))).thenReturn(maxMin);

        maximoMinimoService.desactivar(1L, 1L);

        assertFalse(maxMin.getActivo());
    }
}

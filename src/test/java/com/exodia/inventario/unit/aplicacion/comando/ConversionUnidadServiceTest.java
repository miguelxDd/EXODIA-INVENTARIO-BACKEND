package com.exodia.inventario.unit.aplicacion.comando;

import com.exodia.inventario.aplicacion.comando.impl.ConversionUnidadServiceImpl;
import com.exodia.inventario.domain.modelo.catalogo.ConversionUnidad;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;
import com.exodia.inventario.domain.modelo.catalogo.Unidad;
import com.exodia.inventario.excepcion.EntidadNoEncontradaException;
import com.exodia.inventario.interfaz.dto.peticion.CrearConversionUnidadRequest;
import com.exodia.inventario.interfaz.dto.respuesta.ConversionUnidadResponse;
import com.exodia.inventario.interfaz.mapeador.ConversionUnidadMapeador;
import com.exodia.inventario.repositorio.catalogo.ConversionUnidadRepository;
import com.exodia.inventario.repositorio.catalogo.EmpresaRepository;
import com.exodia.inventario.repositorio.catalogo.UnidadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class ConversionUnidadServiceTest {

    @Mock private ConversionUnidadRepository conversionUnidadRepository;
    @Mock private UnidadRepository unidadRepository;
    @Mock private EmpresaRepository empresaRepository;
    @Mock private ConversionUnidadMapeador conversionUnidadMapeador;

    @InjectMocks
    private ConversionUnidadServiceImpl conversionUnidadService;

    private Empresa empresa;
    private Unidad unidadOrigen;
    private Unidad unidadDestino;

    @BeforeEach
    void setUp() {
        empresa = Empresa.builder().codigo("EMP1").nombre("Test").build();
        empresa.setId(1L);

        unidadOrigen = Unidad.builder().empresa(empresa).codigo("KG").nombre("Kilogramo").abreviatura("kg").build();
        unidadOrigen.setId(1L);
        unidadOrigen.setActivo(true);

        unidadDestino = Unidad.builder().empresa(empresa).codigo("GR").nombre("Gramo").abreviatura("g").build();
        unidadDestino.setId(2L);
        unidadDestino.setActivo(true);
    }

    @Test
    void deberiaCrearConversionExitosamente() {
        CrearConversionUnidadRequest request = new CrearConversionUnidadRequest(
                1L, 2L, new BigDecimal("1000"), "MULTIPLICAR", null);

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(unidadRepository.findById(1L)).thenReturn(Optional.of(unidadOrigen));
        when(unidadRepository.findById(2L)).thenReturn(Optional.of(unidadDestino));
        when(conversionUnidadRepository.save(any(ConversionUnidad.class))).thenAnswer(inv -> {
            ConversionUnidad c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });
        when(conversionUnidadMapeador.toResponse(any(ConversionUnidad.class)))
                .thenReturn(new ConversionUnidadResponse(1L, 1L, "KG", 2L, "GR",
                        new BigDecimal("1000"), "MULTIPLICAR", null));

        ConversionUnidadResponse response = conversionUnidadService.crear(1L, request);

        assertNotNull(response);
        assertEquals(new BigDecimal("1000"), response.factorConversion());
        verify(conversionUnidadRepository).save(any(ConversionUnidad.class));
    }

    @Test
    void deberiaFallarSiEmpresaNoExiste() {
        when(empresaRepository.findById(999L)).thenReturn(Optional.empty());

        CrearConversionUnidadRequest request = new CrearConversionUnidadRequest(
                1L, 2L, new BigDecimal("1000"), "MULTIPLICAR", null);

        assertThrows(EntidadNoEncontradaException.class,
                () -> conversionUnidadService.crear(999L, request));
    }

    @Test
    void deberiaFallarSiUnidadOrigenNoExiste() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(unidadRepository.findById(999L)).thenReturn(Optional.empty());

        CrearConversionUnidadRequest request = new CrearConversionUnidadRequest(
                999L, 2L, new BigDecimal("1000"), "MULTIPLICAR", null);

        assertThrows(EntidadNoEncontradaException.class,
                () -> conversionUnidadService.crear(1L, request));
    }

    @Test
    void deberiaFallarSiUnidadDestinoNoExiste() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(unidadRepository.findById(1L)).thenReturn(Optional.of(unidadOrigen));
        when(unidadRepository.findById(999L)).thenReturn(Optional.empty());

        CrearConversionUnidadRequest request = new CrearConversionUnidadRequest(
                1L, 999L, new BigDecimal("1000"), "MULTIPLICAR", null);

        assertThrows(EntidadNoEncontradaException.class,
                () -> conversionUnidadService.crear(1L, request));
    }

    @Test
    void deberiaFallarSiUnidadOrigenEsDeOtraEmpresa() {
        Empresa otraEmpresa = Empresa.builder().codigo("EMP2").nombre("Otra").build();
        otraEmpresa.setId(2L);
        unidadOrigen.setEmpresa(otraEmpresa);

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(unidadRepository.findById(1L)).thenReturn(Optional.of(unidadOrigen));

        CrearConversionUnidadRequest request = new CrearConversionUnidadRequest(
                1L, 2L, new BigDecimal("1000"), "MULTIPLICAR", null);

        assertThrows(EntidadNoEncontradaException.class,
                () -> conversionUnidadService.crear(1L, request));
    }

    @Test
    void deberiaListarConversionesPorEmpresa() {
        ConversionUnidad conversion = ConversionUnidad.builder()
                .empresa(empresa).unidadOrigen(unidadOrigen).unidadDestino(unidadDestino)
                .factorConversion(new BigDecimal("1000")).build();
        conversion.setId(1L);
        conversion.setActivo(true);

        when(conversionUnidadRepository.findByEmpresaIdAndActivoTrue(1L)).thenReturn(List.of(conversion));
        when(conversionUnidadMapeador.toResponseList(any())).thenReturn(List.of(
                new ConversionUnidadResponse(1L, 1L, "KG", 2L, "GR",
                        new BigDecimal("1000"), "MULTIPLICAR", null)));

        List<ConversionUnidadResponse> response = conversionUnidadService.listarPorEmpresa(1L);

        assertEquals(1, response.size());
    }

    @Test
    void deberiaDesactivarConversion() {
        ConversionUnidad conversion = ConversionUnidad.builder()
                .empresa(empresa).unidadOrigen(unidadOrigen).unidadDestino(unidadDestino)
                .factorConversion(new BigDecimal("1000")).build();
        conversion.setId(1L);
        conversion.setActivo(true);

        when(conversionUnidadRepository.findById(1L)).thenReturn(Optional.of(conversion));
        when(conversionUnidadRepository.save(any(ConversionUnidad.class))).thenReturn(conversion);

        conversionUnidadService.desactivar(1L, 1L);

        assertFalse(conversion.getActivo());
    }
}

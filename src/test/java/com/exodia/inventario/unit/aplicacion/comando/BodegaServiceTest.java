package com.exodia.inventario.unit.aplicacion.comando;

import com.exodia.inventario.aplicacion.comando.impl.BodegaServiceImpl;
import com.exodia.inventario.domain.modelo.catalogo.Bodega;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;
import com.exodia.inventario.excepcion.EntidadNoEncontradaException;
import com.exodia.inventario.interfaz.dto.peticion.ActualizarBodegaRequest;
import com.exodia.inventario.interfaz.dto.peticion.CrearBodegaRequest;
import com.exodia.inventario.interfaz.dto.respuesta.BodegaResponse;
import com.exodia.inventario.interfaz.mapeador.BodegaMapeador;
import com.exodia.inventario.repositorio.catalogo.BodegaRepository;
import com.exodia.inventario.repositorio.catalogo.EmpresaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class BodegaServiceTest {

    @Mock private BodegaRepository bodegaRepository;
    @Mock private EmpresaRepository empresaRepository;
    @Mock private BodegaMapeador bodegaMapeador;

    @InjectMocks
    private BodegaServiceImpl bodegaService;

    private Empresa empresa;
    private Bodega bodega;

    @BeforeEach
    void setUp() {
        empresa = Empresa.builder().codigo("EMP1").nombre("Test").build();
        empresa.setId(1L);

        bodega = Bodega.builder()
                .empresa(empresa).codigo("BOD1").nombre("Bodega Central")
                .direccion("Dir").ciudad("Ciudad").pais("Pais")
                .esProductoTerminado(false).esConsignacion(false)
                .build();
        bodega.setId(1L);
        bodega.setActivo(true);
    }

    @Test
    void deberiaCrearBodegaExitosamente() {
        CrearBodegaRequest request = new CrearBodegaRequest(
                "BOD1", "Bodega Central", "Dir", "Ciudad", "Pais", null, null);

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(bodegaRepository.save(any(Bodega.class))).thenAnswer(inv -> {
            Bodega b = inv.getArgument(0);
            b.setId(1L);
            return b;
        });
        when(bodegaMapeador.toResponse(any(Bodega.class)))
                .thenReturn(new BodegaResponse(1L, "BOD1", "Bodega Central", "Dir", "Ciudad", "Pais", false, false, null));

        BodegaResponse response = bodegaService.crear(1L, request);

        assertNotNull(response);
        assertEquals("BOD1", response.codigo());
        verify(bodegaRepository).save(any(Bodega.class));
    }

    @Test
    void deberiaFallarSiEmpresaNoExisteAlCrear() {
        when(empresaRepository.findById(999L)).thenReturn(Optional.empty());

        CrearBodegaRequest request = new CrearBodegaRequest(
                "BOD1", "Bodega", null, null, null, null, null);

        assertThrows(EntidadNoEncontradaException.class,
                () -> bodegaService.crear(999L, request));
    }

    @Test
    void deberiaObtenerBodegaPorId() {
        when(bodegaRepository.findById(1L)).thenReturn(Optional.of(bodega));
        when(bodegaMapeador.toResponse(bodega))
                .thenReturn(new BodegaResponse(1L, "BOD1", "Bodega Central", "Dir", "Ciudad", "Pais", false, false, null));

        BodegaResponse response = bodegaService.obtenerPorId(1L, 1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
    }

    @Test
    void deberiaFallarSiBodegaNoExisteAlObtener() {
        when(bodegaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntidadNoEncontradaException.class,
                () -> bodegaService.obtenerPorId(1L, 999L));
    }

    @Test
    void deberiaFallarSiBodegaEsDeOtraEmpresa() {
        Empresa otraEmpresa = Empresa.builder().codigo("EMP2").nombre("Otra").build();
        otraEmpresa.setId(2L);
        bodega.setEmpresa(otraEmpresa);

        when(bodegaRepository.findById(1L)).thenReturn(Optional.of(bodega));

        assertThrows(EntidadNoEncontradaException.class,
                () -> bodegaService.obtenerPorId(1L, 1L));
    }

    @Test
    void deberiaListarBodegasPorEmpresa() {
        when(bodegaRepository.findByEmpresaIdAndActivoTrue(1L)).thenReturn(List.of(bodega));
        when(bodegaMapeador.toResponseList(any())).thenReturn(List.of(
                new BodegaResponse(1L, "BOD1", "Bodega Central", "Dir", "Ciudad", "Pais", false, false, null)));

        List<BodegaResponse> response = bodegaService.listarPorEmpresa(1L);

        assertEquals(1, response.size());
    }

    @Test
    void deberiaActualizarBodegaSelectivamente() {
        when(bodegaRepository.findById(1L)).thenReturn(Optional.of(bodega));
        when(bodegaRepository.save(any(Bodega.class))).thenReturn(bodega);
        when(bodegaMapeador.toResponse(any(Bodega.class)))
                .thenReturn(new BodegaResponse(1L, "BOD1", "Nuevo Nombre", "Dir", "Ciudad", "Pais", false, false, null));

        ActualizarBodegaRequest request = new ActualizarBodegaRequest(
                "Nuevo Nombre", null, null, null, null, null);

        BodegaResponse response = bodegaService.actualizar(1L, 1L, request);

        assertNotNull(response);
        assertEquals("Nuevo Nombre", response.nombre());
        verify(bodegaRepository).save(any(Bodega.class));
    }

    @Test
    void deberiaDesactivarBodega() {
        when(bodegaRepository.findById(1L)).thenReturn(Optional.of(bodega));
        when(bodegaRepository.save(any(Bodega.class))).thenReturn(bodega);

        bodegaService.desactivar(1L, 1L);

        assertFalse(bodega.getActivo());
        verify(bodegaRepository).save(bodega);
    }
}

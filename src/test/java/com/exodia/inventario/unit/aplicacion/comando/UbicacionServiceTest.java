package com.exodia.inventario.unit.aplicacion.comando;

import com.exodia.inventario.aplicacion.comando.impl.UbicacionServiceImpl;
import com.exodia.inventario.domain.modelo.catalogo.Bodega;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;
import com.exodia.inventario.domain.modelo.catalogo.Ubicacion;
import com.exodia.inventario.excepcion.EntidadNoEncontradaException;
import com.exodia.inventario.interfaz.dto.peticion.CrearUbicacionRequest;
import com.exodia.inventario.interfaz.dto.respuesta.UbicacionResponse;
import com.exodia.inventario.interfaz.mapeador.UbicacionMapeador;
import com.exodia.inventario.repositorio.catalogo.BodegaRepository;
import com.exodia.inventario.repositorio.catalogo.UbicacionRepository;
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
class UbicacionServiceTest {

    @Mock private UbicacionRepository ubicacionRepository;
    @Mock private BodegaRepository bodegaRepository;
    @Mock private UbicacionMapeador ubicacionMapeador;

    @InjectMocks
    private UbicacionServiceImpl ubicacionService;

    private Empresa empresa;
    private Bodega bodega;
    private Ubicacion ubicacion;

    @BeforeEach
    void setUp() {
        empresa = Empresa.builder().codigo("EMP1").nombre("Test").build();
        empresa.setId(1L);

        bodega = Bodega.builder().empresa(empresa).codigo("BOD1").nombre("Bodega").build();
        bodega.setId(1L);
        bodega.setActivo(true);

        ubicacion = Ubicacion.builder().bodega(bodega).codigo("UB01").nombre("Ubicacion 1").build();
        ubicacion.setId(1L);
        ubicacion.setActivo(true);
    }

    @Test
    void deberiaCrearUbicacionExitosamente() {
        CrearUbicacionRequest request = new CrearUbicacionRequest(1L, "UB01", "Ubicacion 1", null, null);

        when(bodegaRepository.findById(1L)).thenReturn(Optional.of(bodega));
        when(ubicacionRepository.save(any(Ubicacion.class))).thenAnswer(inv -> {
            Ubicacion u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(ubicacionMapeador.toResponse(any(Ubicacion.class)))
                .thenReturn(new UbicacionResponse(1L, 1L, "UB01", "Ubicacion 1", null, "GENERAL"));

        UbicacionResponse response = ubicacionService.crear(1L, request);

        assertNotNull(response);
        assertEquals("UB01", response.codigo());
        assertEquals("GENERAL", response.tipoUbicacion());
    }

    @Test
    void deberiaFallarSiBodegaNoExisteAlCrear() {
        when(bodegaRepository.findById(999L)).thenReturn(Optional.empty());

        CrearUbicacionRequest request = new CrearUbicacionRequest(999L, "UB01", "Ubicacion", null, null);

        assertThrows(EntidadNoEncontradaException.class,
                () -> ubicacionService.crear(1L, request));
    }

    @Test
    void deberiaFallarSiBodegaEsDeOtraEmpresa() {
        Empresa otraEmpresa = Empresa.builder().codigo("EMP2").nombre("Otra").build();
        otraEmpresa.setId(2L);
        Bodega bodegaOtra = Bodega.builder().empresa(otraEmpresa).codigo("BOD2").nombre("Otra").build();
        bodegaOtra.setId(2L);
        bodegaOtra.setActivo(true);

        when(bodegaRepository.findById(2L)).thenReturn(Optional.of(bodegaOtra));

        CrearUbicacionRequest request = new CrearUbicacionRequest(2L, "UB01", "Ubicacion", null, null);

        assertThrows(EntidadNoEncontradaException.class,
                () -> ubicacionService.crear(1L, request));
    }

    @Test
    void deberiaObtenerUbicacionPorId() {
        when(ubicacionRepository.findById(1L)).thenReturn(Optional.of(ubicacion));
        when(ubicacionMapeador.toResponse(ubicacion))
                .thenReturn(new UbicacionResponse(1L, 1L, "UB01", "Ubicacion 1", null, "GENERAL"));

        UbicacionResponse response = ubicacionService.obtenerPorId(1L, 1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
    }

    @Test
    void deberiaListarUbicacionesPorBodega() {
        when(bodegaRepository.findById(1L)).thenReturn(Optional.of(bodega));
        when(ubicacionRepository.findByBodegaIdAndActivoTrue(1L)).thenReturn(List.of(ubicacion));
        when(ubicacionMapeador.toResponseList(any())).thenReturn(List.of(
                new UbicacionResponse(1L, 1L, "UB01", "Ubicacion 1", null, "GENERAL")));

        List<UbicacionResponse> response = ubicacionService.listarPorBodega(1L, 1L);

        assertEquals(1, response.size());
    }

    @Test
    void deberiaDesactivarUbicacion() {
        when(ubicacionRepository.findById(1L)).thenReturn(Optional.of(ubicacion));
        when(ubicacionRepository.save(any(Ubicacion.class))).thenReturn(ubicacion);

        ubicacionService.desactivar(1L, 1L);

        assertFalse(ubicacion.getActivo());
        verify(ubicacionRepository).save(ubicacion);
    }
}

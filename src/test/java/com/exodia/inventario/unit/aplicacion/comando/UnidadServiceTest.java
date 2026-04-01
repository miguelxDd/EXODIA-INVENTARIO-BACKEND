package com.exodia.inventario.unit.aplicacion.comando;

import com.exodia.inventario.aplicacion.comando.impl.UnidadServiceImpl;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;
import com.exodia.inventario.domain.modelo.catalogo.Unidad;
import com.exodia.inventario.excepcion.EntidadNoEncontradaException;
import com.exodia.inventario.interfaz.dto.peticion.ActualizarUnidadRequest;
import com.exodia.inventario.interfaz.dto.peticion.CrearUnidadRequest;
import com.exodia.inventario.interfaz.dto.respuesta.UnidadResponse;
import com.exodia.inventario.interfaz.mapeador.UnidadMapeador;
import com.exodia.inventario.repositorio.catalogo.EmpresaRepository;
import com.exodia.inventario.repositorio.catalogo.UnidadRepository;
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
class UnidadServiceTest {

    @Mock private UnidadRepository unidadRepository;
    @Mock private EmpresaRepository empresaRepository;
    @Mock private UnidadMapeador unidadMapeador;

    @InjectMocks
    private UnidadServiceImpl unidadService;

    private Empresa empresa;
    private Unidad unidad;

    @BeforeEach
    void setUp() {
        empresa = Empresa.builder().codigo("EMP1").nombre("Test").build();
        empresa.setId(1L);

        unidad = Unidad.builder().empresa(empresa).codigo("KG").nombre("Kilogramo").abreviatura("kg").build();
        unidad.setId(1L);
        unidad.setActivo(true);
    }

    @Test
    void deberiaCrearUnidadExitosamente() {
        CrearUnidadRequest request = new CrearUnidadRequest("KG", "Kilogramo", "kg");

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(unidadRepository.save(any(Unidad.class))).thenAnswer(inv -> {
            Unidad u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(unidadMapeador.toResponse(any(Unidad.class)))
                .thenReturn(new UnidadResponse(1L, "KG", "Kilogramo", "kg"));

        UnidadResponse response = unidadService.crear(1L, request);

        assertNotNull(response);
        assertEquals("KG", response.codigo());
        verify(unidadRepository).save(any(Unidad.class));
    }

    @Test
    void deberiaFallarSiEmpresaNoExisteAlCrear() {
        when(empresaRepository.findById(999L)).thenReturn(Optional.empty());

        CrearUnidadRequest request = new CrearUnidadRequest("KG", "Kilogramo", "kg");

        assertThrows(EntidadNoEncontradaException.class,
                () -> unidadService.crear(999L, request));
    }

    @Test
    void deberiaObtenerUnidadPorId() {
        when(unidadRepository.findById(1L)).thenReturn(Optional.of(unidad));
        when(unidadMapeador.toResponse(unidad))
                .thenReturn(new UnidadResponse(1L, "KG", "Kilogramo", "kg"));

        UnidadResponse response = unidadService.obtenerPorId(1L, 1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
    }

    @Test
    void deberiaFallarSiUnidadEsDeOtraEmpresa() {
        Empresa otraEmpresa = Empresa.builder().codigo("EMP2").nombre("Otra").build();
        otraEmpresa.setId(2L);
        unidad.setEmpresa(otraEmpresa);

        when(unidadRepository.findById(1L)).thenReturn(Optional.of(unidad));

        assertThrows(EntidadNoEncontradaException.class,
                () -> unidadService.obtenerPorId(1L, 1L));
    }

    @Test
    void deberiaListarUnidadesPorEmpresa() {
        when(unidadRepository.findByEmpresaIdAndActivoTrue(1L)).thenReturn(List.of(unidad));
        when(unidadMapeador.toResponseList(any())).thenReturn(List.of(
                new UnidadResponse(1L, "KG", "Kilogramo", "kg")));

        List<UnidadResponse> response = unidadService.listarPorEmpresa(1L);

        assertEquals(1, response.size());
    }

    @Test
    void deberiaActualizarUnidadSelectivamente() {
        when(unidadRepository.findById(1L)).thenReturn(Optional.of(unidad));
        when(unidadRepository.save(any(Unidad.class))).thenReturn(unidad);
        when(unidadMapeador.toResponse(any(Unidad.class)))
                .thenReturn(new UnidadResponse(1L, "KG", "Nuevo Nombre", "kg"));

        ActualizarUnidadRequest request = new ActualizarUnidadRequest("Nuevo Nombre", null);

        UnidadResponse response = unidadService.actualizar(1L, 1L, request);

        assertNotNull(response);
        verify(unidadRepository).save(any(Unidad.class));
    }

    @Test
    void deberiaDesactivarUnidad() {
        when(unidadRepository.findById(1L)).thenReturn(Optional.of(unidad));
        when(unidadRepository.save(any(Unidad.class))).thenReturn(unidad);

        unidadService.desactivar(1L, 1L);

        assertFalse(unidad.getActivo());
        verify(unidadRepository).save(unidad);
    }
}

package com.exodia.inventario.unit.aplicacion.consulta;

import com.exodia.inventario.aplicacion.consulta.impl.KardexQueryServiceImpl;
import com.exodia.inventario.domain.modelo.contenedor.Operacion;
import com.exodia.inventario.repositorio.contenedor.OperacionRepository;
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

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class KardexQueryServiceTest {

    @Mock
    private OperacionRepository operacionRepository;

    @InjectMocks
    private KardexQueryServiceImpl kardexQueryService;

    @Test
    void deberiaConsultarKardexSinFiltros() {
        Pageable pageable = PageRequest.of(0, 20);
        Operacion operacion = new Operacion();
        Page<Operacion> mockPage = new PageImpl<>(List.of(operacion));

        when(operacionRepository.findKardex(1L, null, null, null, null, null, null, pageable))
                .thenReturn(mockPage);

        Page<Operacion> resultado = kardexQueryService.consultarKardex(
                1L, null, null, null, null, null, null, pageable);

        assertEquals(1, resultado.getTotalElements());
    }

    @Test
    void deberiaConsultarKardexConFiltros() {
        Pageable pageable = PageRequest.of(0, 10);
        OffsetDateTime desde = OffsetDateTime.now().minusDays(7);
        OffsetDateTime hasta = OffsetDateTime.now();
        Page<Operacion> mockPage = new PageImpl<>(List.of());

        when(operacionRepository.findKardex(1L, 5L, "INV00000001", 100L, 1L, desde, hasta, pageable))
                .thenReturn(mockPage);

        Page<Operacion> resultado = kardexQueryService.consultarKardex(
                1L, 5L, "INV00000001", 100L, 1L, desde, hasta, pageable);

        assertEquals(0, resultado.getTotalElements());
        verify(operacionRepository).findKardex(1L, 5L, "INV00000001", 100L, 1L, desde, hasta, pageable);
    }

    @Test
    void deberiaConsultarKardexPorContenedor() {
        Pageable pageable = PageRequest.of(0, 20);
        Operacion op1 = new Operacion();
        Operacion op2 = new Operacion();
        Page<Operacion> mockPage = new PageImpl<>(List.of(op1, op2));

        when(operacionRepository.findKardex(1L, 5L, null, null, null, null, null, pageable))
                .thenReturn(mockPage);

        Page<Operacion> resultado = kardexQueryService.consultarKardex(
                1L, 5L, null, null, null, null, null, pageable);

        assertEquals(2, resultado.getTotalElements());
    }

    @Test
    void deberiaConsultarKardexPorRangoDeFechas() {
        Pageable pageable = PageRequest.of(0, 20);
        OffsetDateTime desde = OffsetDateTime.now().minusDays(30);
        OffsetDateTime hasta = OffsetDateTime.now();
        Page<Operacion> mockPage = new PageImpl<>(List.of());

        when(operacionRepository.findKardex(1L, null, null, null, null, desde, hasta, pageable))
                .thenReturn(mockPage);

        Page<Operacion> resultado = kardexQueryService.consultarKardex(
                1L, null, null, null, null, desde, hasta, pageable);

        assertNotNull(resultado);
        verify(operacionRepository).findKardex(1L, null, null, null, null, desde, hasta, pageable);
    }
}

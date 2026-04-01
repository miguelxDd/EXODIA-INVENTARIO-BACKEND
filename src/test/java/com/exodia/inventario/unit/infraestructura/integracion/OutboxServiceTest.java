package com.exodia.inventario.unit.infraestructura.integracion;

import com.exodia.inventario.domain.enums.EstadoOutbox;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;
import com.exodia.inventario.domain.modelo.integracion.EventoOutbox;
import com.exodia.inventario.infraestructura.integracion.OutboxService;
import com.exodia.inventario.repositorio.catalogo.EmpresaRepository;
import com.exodia.inventario.repositorio.integracion.EventoOutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class OutboxServiceTest {

    @Mock private EventoOutboxRepository eventoOutboxRepository;
    @Mock private EmpresaRepository empresaRepository;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks
    private OutboxService outboxService;

    @AfterEach
    void limpiarMdc() {
        MDC.clear();
    }

    @Test
    void deberiaUsarCorrelationIdDesdeMdcCuandoNoSeEnviaUnoExplicito() throws JsonProcessingException {
        Empresa empresa = crearEmpresa();
        MDC.put("correlationId", "corr-123");

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"ok\":true}");

        outboxService.registrar(1L, "Recepcion", 99L, "inventario.recepcion.confirmada", List.of("x"), null);

        ArgumentCaptor<EventoOutbox> captor = ArgumentCaptor.forClass(EventoOutbox.class);
        verify(eventoOutboxRepository).save(captor.capture());

        EventoOutbox guardado = captor.getValue();
        assertEquals("corr-123", guardado.getCorrelationId());
        assertEquals(EstadoOutbox.PENDIENTE, guardado.getEstado());
    }

    @Test
    void deberiaGenerarCorrelationIdCuandoNoHayContexto() throws JsonProcessingException {
        Empresa empresa = crearEmpresa();

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"ok\":true}");

        outboxService.registrar(1L, "Picking", 77L, "inventario.picking.completado", List.of("x"), null);

        ArgumentCaptor<EventoOutbox> captor = ArgumentCaptor.forClass(EventoOutbox.class);
        verify(eventoOutboxRepository).save(captor.capture());

        EventoOutbox guardado = captor.getValue();
        assertNotNull(guardado.getCorrelationId());
        assertFalse(guardado.getCorrelationId().isBlank());
        assertTrue(guardado.getCorrelationId().length() <= 100);
    }

    private Empresa crearEmpresa() {
        Empresa empresa = Empresa.builder().codigo("EMP1").nombre("Empresa Test").build();
        empresa.setId(1L);
        return empresa;
    }
}

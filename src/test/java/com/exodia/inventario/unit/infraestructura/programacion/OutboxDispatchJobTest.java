package com.exodia.inventario.unit.infraestructura.programacion;

import com.exodia.inventario.domain.enums.EstadoOutbox;
import com.exodia.inventario.domain.modelo.integracion.EventoOutbox;
import com.exodia.inventario.infraestructura.integracion.OutboxRelay;
import com.exodia.inventario.infraestructura.integracion.OutboxService;
import com.exodia.inventario.infraestructura.programacion.OutboxDispatchJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class OutboxDispatchJobTest {

    @Mock private OutboxService outboxService;
    @Mock private OutboxRelay outboxRelay;

    @InjectMocks
    private OutboxDispatchJob outboxDispatchJob;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(outboxDispatchJob, "maxIntentos", 5);
    }

    @Test
    void deberiaPublicarEventosPendientesYMarcarComoPublicados() {
        EventoOutbox eventoOutbox = crearEvento(10L, EstadoOutbox.FALLIDO);

        when(outboxService.obtenerPendientesOReintentables(5)).thenReturn(List.of(eventoOutbox));

        outboxDispatchJob.publicarPendientes();

        verify(outboxRelay).publicar(eventoOutbox);
        verify(outboxService).marcarPublicado(10L);
    }

    @Test
    void deberiaMarcarFallidoCuandoElRelayLanzaExcepcion() {
        EventoOutbox eventoOutbox = crearEvento(11L, EstadoOutbox.PENDIENTE);

        when(outboxService.obtenerPendientesOReintentables(5)).thenReturn(List.of(eventoOutbox));
        doThrow(new IllegalStateException("relay caido")).when(outboxRelay).publicar(eventoOutbox);
        doNothing().when(outboxService).marcarFallido(any(), any());

        outboxDispatchJob.publicarPendientes();

        verify(outboxRelay).publicar(eventoOutbox);
        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Exception> captor = ArgumentCaptor.forClass(Exception.class);
        verify(outboxService).marcarFallido(idCaptor.capture(), captor.capture());
        assertEquals(11L, idCaptor.getValue());
        assertEquals("relay caido", captor.getValue().getMessage());
    }

    @Test
    void noDeberiaIntentarPublicarSiNoHayEventos() {
        when(outboxService.obtenerPendientesOReintentables(5)).thenReturn(List.of());

        outboxDispatchJob.publicarPendientes();

        verify(outboxService).obtenerPendientesOReintentables(5);
        verifyNoMoreInteractions(outboxRelay);
    }

    private EventoOutbox crearEvento(Long id, EstadoOutbox estado) {
        EventoOutbox eventoOutbox = EventoOutbox.builder()
                .aggregateType("Recepcion")
                .aggregateId(99L)
                .eventType("inventario.recepcion.confirmada")
                .payloadJson("{}")
                .estado(estado)
                .build();
        eventoOutbox.setId(id);
        return eventoOutbox;
    }
}

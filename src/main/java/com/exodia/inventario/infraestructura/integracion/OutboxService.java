package com.exodia.inventario.infraestructura.integracion;

import com.exodia.inventario.domain.enums.EstadoOutbox;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;
import com.exodia.inventario.domain.modelo.integracion.EventoOutbox;
import com.exodia.inventario.excepcion.EntidadNoEncontradaException;
import com.exodia.inventario.repositorio.catalogo.EmpresaRepository;
import com.exodia.inventario.repositorio.integracion.EventoOutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxService {

    private final EventoOutboxRepository eventoOutboxRepository;
    private final EmpresaRepository empresaRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void registrar(Long empresaId,
                          String aggregateType,
                          Long aggregateId,
                          String eventType,
                          Object payload,
                          String correlationId) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EntidadNoEncontradaException("Empresa", empresaId));

        EventoOutbox eventoOutbox = EventoOutbox.builder()
                .empresa(empresa)
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payloadJson(serializar(payload))
                .correlationId(correlationId)
                .estado(EstadoOutbox.PENDIENTE)
                .build();

        eventoOutboxRepository.save(eventoOutbox);
    }

    @Transactional(readOnly = true)
    public List<EventoOutbox> obtenerPendientes() {
        return eventoOutboxRepository.findTop100ByEstadoOrderByCreadoEnAsc(EstadoOutbox.PENDIENTE);
    }

    @Transactional
    public void marcarPublicado(Long eventoId) {
        EventoOutbox eventoOutbox = buscar(eventoId);
        eventoOutbox.setEstado(EstadoOutbox.PUBLICADO);
        eventoOutbox.setPublicadoEn(OffsetDateTime.now());
        eventoOutbox.setUltimoError(null);
        eventoOutbox.setIntentos(eventoOutbox.getIntentos() + 1);
        eventoOutboxRepository.save(eventoOutbox);
    }

    @Transactional
    public void marcarFallido(Long eventoId, Exception exception) {
        EventoOutbox eventoOutbox = buscar(eventoId);
        eventoOutbox.setEstado(EstadoOutbox.FALLIDO);
        eventoOutbox.setIntentos(eventoOutbox.getIntentos() + 1);
        eventoOutbox.setUltimoError(exception.getMessage());
        eventoOutboxRepository.save(eventoOutbox);
    }

    private EventoOutbox buscar(Long eventoId) {
        return eventoOutboxRepository.findById(eventoId)
                .orElseThrow(() -> new EntidadNoEncontradaException("EventoOutbox", eventoId));
    }

    private String serializar(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.error("No se pudo serializar payload de outbox: {}", e.getMessage());
            throw new IllegalStateException("No se pudo serializar evento de outbox", e);
        }
    }
}

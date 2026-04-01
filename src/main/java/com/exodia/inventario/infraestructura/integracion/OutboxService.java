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
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxService {

    private static final String CORRELATION_ID_KEY = "correlationId";
    private static final int MAX_CORRELATION_ID_LENGTH = 100;
    private static final int MAX_ERROR_LENGTH = 2000;

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
                .correlationId(resolverCorrelationId(correlationId))
                .estado(EstadoOutbox.PENDIENTE)
                .build();

        eventoOutboxRepository.save(eventoOutbox);
    }

    @Transactional(readOnly = true)
    public List<EventoOutbox> obtenerPendientesOReintentables(int maxIntentos) {
        return eventoOutboxRepository.findTop100ByEstadoInAndIntentosLessThanOrderByCreadoEnAsc(
                List.of(EstadoOutbox.PENDIENTE, EstadoOutbox.FALLIDO),
                maxIntentos);
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
        eventoOutbox.setUltimoError(truncar(exception.getMessage(), MAX_ERROR_LENGTH));
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

    private String resolverCorrelationId(String correlationId) {
        String candidato = correlationId;
        if (candidato == null || candidato.isBlank()) {
            candidato = MDC.get(CORRELATION_ID_KEY);
        }
        if (candidato == null || candidato.isBlank()) {
            candidato = UUID.randomUUID().toString();
        }
        return truncar(candidato, MAX_CORRELATION_ID_LENGTH);
    }

    private String truncar(String valor, int longitudMaxima) {
        if (valor == null || valor.length() <= longitudMaxima) {
            return valor;
        }
        return valor.substring(0, longitudMaxima);
    }
}

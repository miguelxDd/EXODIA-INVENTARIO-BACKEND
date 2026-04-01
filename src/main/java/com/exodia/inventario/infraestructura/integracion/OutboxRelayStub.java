package com.exodia.inventario.infraestructura.integracion;

import com.exodia.inventario.domain.modelo.integracion.EventoOutbox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OutboxRelayStub implements OutboxRelay {

    @Override
    public void publicar(EventoOutbox eventoOutbox) {
        log.info("Outbox relay stub: eventType={}, aggregateType={}, aggregateId={}, correlationId={}",
                eventoOutbox.getEventType(), eventoOutbox.getAggregateType(),
                eventoOutbox.getAggregateId(), eventoOutbox.getCorrelationId());
    }
}

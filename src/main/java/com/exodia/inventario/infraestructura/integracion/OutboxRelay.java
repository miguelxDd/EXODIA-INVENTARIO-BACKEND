package com.exodia.inventario.infraestructura.integracion;

import com.exodia.inventario.domain.modelo.integracion.EventoOutbox;

/**
 * Relay desacoplado del outbox.
 * En produccion podria publicar a Kafka, RabbitMQ o webhooks.
 */
public interface OutboxRelay {

    void publicar(EventoOutbox eventoOutbox);
}

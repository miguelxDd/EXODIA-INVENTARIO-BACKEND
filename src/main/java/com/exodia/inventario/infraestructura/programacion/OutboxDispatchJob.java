package com.exodia.inventario.infraestructura.programacion;

import com.exodia.inventario.domain.modelo.integracion.EventoOutbox;
import com.exodia.inventario.infraestructura.integracion.OutboxRelay;
import com.exodia.inventario.infraestructura.integracion.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxDispatchJob {

    private final OutboxService outboxService;
    private final OutboxRelay outboxRelay;

    @Scheduled(cron = "0 */1 * * * ?")
    @Transactional
    public void publicarPendientes() {
        List<EventoOutbox> pendientes = outboxService.obtenerPendientes();
        if (pendientes.isEmpty()) {
            return;
        }

        for (EventoOutbox eventoOutbox : pendientes) {
            try {
                outboxRelay.publicar(eventoOutbox);
                outboxService.marcarPublicado(eventoOutbox.getId());
            } catch (Exception e) {
                log.error("Error publicando evento outbox {}: {}", eventoOutbox.getId(), e.getMessage());
                outboxService.marcarFallido(eventoOutbox.getId(), e);
            }
        }
    }
}

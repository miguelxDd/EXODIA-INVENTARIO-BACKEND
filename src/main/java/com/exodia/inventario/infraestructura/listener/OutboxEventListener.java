package com.exodia.inventario.infraestructura.listener;

import com.exodia.inventario.domain.evento.ConteoAplicadoEvent;
import com.exodia.inventario.domain.evento.ConversionInventarioRealizadaEvent;
import com.exodia.inventario.domain.evento.InventarioRecibidoEvent;
import com.exodia.inventario.domain.evento.MermaRegistradaEvent;
import com.exodia.inventario.domain.evento.MovimientoContenedorRealizadoEvent;
import com.exodia.inventario.domain.evento.PickingCompletadoEvent;
import com.exodia.inventario.domain.evento.StockAjustadoEvent;
import com.exodia.inventario.domain.evento.TransferenciaDespachadaEvent;
import com.exodia.inventario.domain.evento.TransferenciaRecibidaEvent;
import com.exodia.inventario.infraestructura.integracion.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OutboxEventListener {

    private final OutboxService outboxService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onInventarioRecibido(InventarioRecibidoEvent event) {
        registrar(event.empresaId(), "Recepcion", event.recepcionId(), "inventario.recepcion.confirmada", event);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onStockAjustado(StockAjustadoEvent event) {
        registrar(event.empresaId(), "Ajuste", event.ajusteId(), "inventario.stock.ajustado", event);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onTransferenciaDespachada(TransferenciaDespachadaEvent event) {
        registrar(event.empresaId(), "Transferencia", event.transferenciaId(),
                "inventario.transferencia.despachada", event);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onTransferenciaRecibida(TransferenciaRecibidaEvent event) {
        registrar(event.empresaId(), "Transferencia", event.transferenciaId(),
                "inventario.transferencia.recibida", event);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onPickingCompletado(PickingCompletadoEvent event) {
        registrar(event.empresaId(), "OrdenPicking", event.ordenPickingId(),
                "inventario.picking.completado", event);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onConteoAplicado(ConteoAplicadoEvent event) {
        registrar(event.empresaId(), "ConteoFisico", event.conteoId(),
                "inventario.conteo.aplicado", event);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onMermaRegistrada(MermaRegistradaEvent event) {
        registrar(event.empresaId(), "RegistroMerma", event.mermaId(),
                "inventario.merma.registrada", event);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onMovimientoRealizado(MovimientoContenedorRealizadoEvent event) {
        registrar(event.empresaId(), "Contenedor", event.contenedorId(),
                "inventario.movimiento.realizado", event);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onConversionRealizada(ConversionInventarioRealizadaEvent event) {
        registrar(event.empresaId(), "Contenedor", event.contenedorOrigenId(),
                "inventario.conversion.realizada", event);
    }

    private void registrar(Long empresaId,
                           String aggregateType,
                           Long aggregateId,
                           String eventType,
                           Object payload) {
        outboxService.registrar(empresaId, aggregateType, aggregateId, eventType, payload, null);
    }
}

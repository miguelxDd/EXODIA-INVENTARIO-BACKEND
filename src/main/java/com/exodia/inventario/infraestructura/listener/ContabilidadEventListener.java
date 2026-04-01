package com.exodia.inventario.infraestructura.listener;

import com.exodia.inventario.domain.evento.ConteoAplicadoEvent;
import com.exodia.inventario.domain.evento.InventarioRecibidoEvent;
import com.exodia.inventario.domain.evento.PickingCompletadoEvent;
import com.exodia.inventario.domain.evento.StockAjustadoEvent;
import com.exodia.inventario.domain.evento.TransferenciaDespachadaEvent;
import com.exodia.inventario.domain.evento.TransferenciaRecibidaEvent;
import com.exodia.inventario.infraestructura.integracion.ContabilidadAdapter;
import com.exodia.inventario.infraestructura.integracion.ContabilidadAdapter.MovimientoContable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContabilidadEventListener {

    private final ContabilidadAdapter contabilidadAdapter;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void onInventarioRecibido(InventarioRecibidoEvent event) {
        notificar(event.empresaId(), "RECEPCION", event.recepcionId(), null,
                String.format("Recepcion con %d lineas en bodega %d",
                        event.totalLineas(), event.bodegaId()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void onStockAjustado(StockAjustadoEvent event) {
        notificar(event.empresaId(), "AJUSTE", event.ajusteId(), null,
                String.format("Ajuste contenedor %d: %s -> %s",
                        event.contenedorId(), event.cantidadAnterior(), event.cantidadNueva()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void onTransferenciaDespachada(TransferenciaDespachadaEvent event) {
        notificar(event.empresaId(), "TRANSFERENCIA_DESPACHO", event.transferenciaId(), null,
                String.format("Despacho bodega %d -> %d, %d contenedores",
                        event.bodegaOrigenId(), event.bodegaDestinoId(),
                        event.contenedorIds().size()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void onTransferenciaRecibida(TransferenciaRecibidaEvent event) {
        notificar(event.empresaId(), "TRANSFERENCIA_RECEPCION", event.transferenciaId(), null,
                String.format("Recepcion transferencia, %d contenedores, completa=%s",
                        event.contenedorIds().size(), event.recepcionCompleta()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void onPickingCompletado(PickingCompletadoEvent event) {
        notificar(event.empresaId(), "PICKING", event.ordenPickingId(), null,
                String.format("Picking completado, %d lineas en bodega %d",
                        event.lineasProcesadas(), event.bodegaId()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void onConteoAplicado(ConteoAplicadoEvent event) {
        notificar(event.empresaId(), "CONTEO_FISICO", event.conteoId(), null,
                String.format("Conteo aplicado: +%d/-%d ajustes en bodega %d",
                        event.ajustesPositivos(), event.ajustesNegativos(), event.bodegaId()));
    }

    private void notificar(Long empresaId, String tipo, Long refId,
                           BigDecimal monto, String descripcion) {
        try {
            contabilidadAdapter.notificarMovimientoInventario(
                    new MovimientoContable(empresaId, tipo, refId, monto, descripcion));
        } catch (Exception e) {
            log.error("Error notificando contabilidad: tipo={}, ref={}: {}",
                    tipo, refId, e.getMessage());
        }
    }
}

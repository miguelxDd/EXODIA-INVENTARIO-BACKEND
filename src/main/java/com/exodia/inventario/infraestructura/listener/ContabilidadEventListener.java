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
import com.exodia.inventario.domain.evento.VentaFacturadaAjustadaEvent;
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

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void onMermaRegistrada(MermaRegistradaEvent event) {
        notificar(event.empresaId(), "MERMA", event.mermaId(), null,
                String.format("Merma registrada contenedor %d, cantidad=%s",
                        event.contenedorId(), event.cantidadMerma()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void onMovimientoContenedor(MovimientoContenedorRealizadoEvent event) {
        notificar(event.empresaId(), "MOVIMIENTO_INTERNO", event.contenedorId(), null,
                String.format("Movimiento interno %d -> %d, cantidad=%s",
                        event.ubicacionOrigenId(), event.ubicacionDestinoId(), event.cantidadMovida()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void onConversionInventario(ConversionInventarioRealizadaEvent event) {
        notificar(event.empresaId(), "CONVERSION_INVENTARIO", event.contenedorOrigenId(), null,
                String.format("Conversion %d -> %d, origen=%s destino=%s",
                        event.unidadOrigenId(), event.unidadDestinoId(),
                        event.cantidadOrigen(), event.cantidadDestino()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void onVentaFacturadaAjustada(VentaFacturadaAjustadaEvent event) {
        notificar(event.empresaId(), "VENTA_AJUSTADA", event.ajusteId(), null,
                String.format("Ajuste por venta %d con %d lineas reales en bodega %d",
                        event.ventaId(), event.lineasProcesadas(), event.bodegaId()));
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

package com.exodia.inventario.infraestructura.listener;

import com.exodia.inventario.domain.evento.ConteoAplicadoEvent;
import com.exodia.inventario.domain.evento.InventarioRecibidoEvent;
import com.exodia.inventario.domain.evento.PickingCompletadoEvent;
import com.exodia.inventario.domain.evento.StockAjustadoEvent;
import com.exodia.inventario.domain.evento.TransferenciaDespachadaEvent;
import com.exodia.inventario.domain.evento.TransferenciaRecibidaEvent;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;
import com.exodia.inventario.domain.modelo.extension.Auditoria;
import com.exodia.inventario.repositorio.catalogo.EmpresaRepository;
import com.exodia.inventario.repositorio.extension.AuditoriaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditoriaEventListener {

    private final AuditoriaRepository auditoriaRepository;
    private final EmpresaRepository empresaRepository;

    @EventListener
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onInventarioRecibido(InventarioRecibidoEvent event) {
        registrar(event.empresaId(), "Recepcion", event.recepcionId(),
                "RECEPCION_CREADA",
                String.format("totalLineas=%d, bodegaId=%d", event.totalLineas(), event.bodegaId()));
    }

    @EventListener
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onStockAjustado(StockAjustadoEvent event) {
        registrar(event.empresaId(), "Ajuste", event.ajusteId(),
                "STOCK_AJUSTADO",
                String.format("contenedorId=%d, productoId=%d, anterior=%s, nuevo=%s",
                        event.contenedorId(), event.productoId(),
                        event.cantidadAnterior(), event.cantidadNueva()));
    }

    @EventListener
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onTransferenciaDespachada(TransferenciaDespachadaEvent event) {
        registrar(event.empresaId(), "Transferencia", event.transferenciaId(),
                "TRANSFERENCIA_DESPACHADA",
                String.format("bodegaOrigenId=%d, bodegaDestinoId=%d, contenedores=%d",
                        event.bodegaOrigenId(), event.bodegaDestinoId(), event.contenedorIds().size()));
    }

    @EventListener
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onTransferenciaRecibida(TransferenciaRecibidaEvent event) {
        registrar(event.empresaId(), "Transferencia", event.transferenciaId(),
                "TRANSFERENCIA_RECIBIDA",
                String.format("contenedoresRecibidos=%d", event.contenedorIds().size()));
    }

    @EventListener
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onPickingCompletado(PickingCompletadoEvent event) {
        registrar(event.empresaId(), "OrdenPicking", event.ordenPickingId(),
                "PICKING_COMPLETADO",
                String.format("bodegaId=%d, lineasProcesadas=%d",
                        event.bodegaId(), event.lineasProcesadas()));
    }

    @EventListener
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onConteoAplicado(ConteoAplicadoEvent event) {
        registrar(event.empresaId(), "ConteoFisico", event.conteoId(),
                "CONTEO_APLICADO",
                String.format("bodegaId=%d, ajustesPositivos=%d, ajustesNegativos=%d",
                        event.bodegaId(), event.ajustesPositivos(), event.ajustesNegativos()));
    }

    private void registrar(Long empresaId, String entidad, Long entidadId,
                           String accion, String datosNuevos) {
        try {
            Empresa empresa = empresaRepository.findById(empresaId).orElse(null);
            if (empresa == null) {
                log.warn("Empresa {} no encontrada para auditoria de {}", empresaId, accion);
                return;
            }

            Auditoria auditoria = Auditoria.builder()
                    .empresa(empresa)
                    .entidad(entidad)
                    .entidadId(entidadId)
                    .accion(accion)
                    .datosNuevos(datosNuevos)
                    .build();

            auditoriaRepository.save(auditoria);
            log.debug("Auditoria registrada: {} {} {}", entidad, entidadId, accion);
        } catch (Exception e) {
            log.error("Error registrando auditoria para {} {}: {}", entidad, entidadId, e.getMessage());
        }
    }
}

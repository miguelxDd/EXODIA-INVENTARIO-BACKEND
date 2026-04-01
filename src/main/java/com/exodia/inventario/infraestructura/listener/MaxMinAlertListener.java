package com.exodia.inventario.infraestructura.listener;

import com.exodia.inventario.aplicacion.consulta.StockQueryService;
import com.exodia.inventario.domain.evento.InventarioRecibidoEvent;
import com.exodia.inventario.domain.evento.MermaRegistradaEvent;
import com.exodia.inventario.domain.evento.PickingCompletadoEvent;
import com.exodia.inventario.domain.evento.StockAjustadoEvent;
import com.exodia.inventario.domain.evento.StockBajoMinimoEvent;
import com.exodia.inventario.domain.evento.TransferenciaDespachadaEvent;
import com.exodia.inventario.domain.evento.TransferenciaRecibidaEvent;
import com.exodia.inventario.domain.evento.VentaFacturadaAjustadaEvent;
import com.exodia.inventario.domain.modelo.extension.MaximoMinimo;
import com.exodia.inventario.repositorio.contenedor.ContenedorRepository;
import com.exodia.inventario.repositorio.extension.MaximoMinimoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class MaxMinAlertListener {

    private final MaximoMinimoRepository maximoMinimoRepository;
    private final ContenedorRepository contenedorRepository;
    private final StockQueryService stockQueryService;
    private final ApplicationEventPublisher eventPublisher;

    @EventListener
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onStockAjustado(StockAjustadoEvent event) {
        verificarMinimo(event.empresaId(), event.productoId(), event.bodegaId());
    }

    @EventListener
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onPickingCompletado(PickingCompletadoEvent event) {
        verificarMinimos(event.empresaId(), event.productoIds(), event.bodegaId());
    }

    @EventListener
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onInventarioRecibido(InventarioRecibidoEvent event) {
        verificarMinimos(event.empresaId(), event.productoIds(), event.bodegaId());
    }

    @EventListener
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onMermaRegistrada(MermaRegistradaEvent event) {
        verificarMinimo(event.empresaId(), event.productoId(), event.bodegaId());
    }

    @EventListener
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onVentaFacturadaAjustada(VentaFacturadaAjustadaEvent event) {
        verificarMinimos(event.empresaId(), event.productoIds(), event.bodegaId());
    }

    @EventListener
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onTransferenciaDespachada(TransferenciaDespachadaEvent event) {
        verificarMinimos(event.empresaId(),
                resolverProductosDesdeContenedores(event.contenedorIds()),
                event.bodegaOrigenId());
    }

    @EventListener
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onTransferenciaRecibida(TransferenciaRecibidaEvent event) {
        verificarMinimos(event.empresaId(),
                resolverProductosDesdeContenedores(event.contenedorIds()),
                event.bodegaDestinoId());
    }

    private void verificarMinimos(Long empresaId, List<Long> productoIds, Long bodegaId) {
        if (productoIds == null || productoIds.isEmpty() || bodegaId == null) {
            return;
        }
        productoIds.stream()
                .filter(id -> id != null)
                .distinct()
                .forEach(productoId -> verificarMinimo(empresaId, productoId, bodegaId));
    }

    private List<Long> resolverProductosDesdeContenedores(List<Long> contenedorIds) {
        if (contenedorIds == null || contenedorIds.isEmpty()) {
            return List.of();
        }
        Set<Long> productoIds = new LinkedHashSet<>();
        contenedorRepository.findAllById(contenedorIds).forEach(contenedor -> {
            if (contenedor.getProductoId() != null) {
                productoIds.add(contenedor.getProductoId());
            }
        });
        return List.copyOf(productoIds);
    }

    private void verificarMinimo(Long empresaId, Long productoId, Long bodegaId) {
        Optional<MaximoMinimo> optMaxMin = maximoMinimoRepository
                .findByEmpresaIdAndProductoIdAndBodegaId(empresaId, productoId, bodegaId);

        if (optMaxMin.isEmpty()) {
            return;
        }

        MaximoMinimo maxMin = optMaxMin.get();
        if (!Boolean.TRUE.equals(maxMin.getActivo())) {
            return;
        }

        BigDecimal stockActual = stockQueryService
                .obtenerStockPorProductoYBodega(empresaId, productoId, bodegaId);

        maxMin.setStockActualCalculado(stockActual);
        maxMin.setUltimaVerificacion(OffsetDateTime.now());
        maximoMinimoRepository.save(maxMin);

        if (stockActual.compareTo(maxMin.getStockMinimo()) < 0) {
            log.warn("Stock bajo minimo: producto={}, bodega={}, actual={}, minimo={}",
                    productoId, bodegaId, stockActual, maxMin.getStockMinimo());

            eventPublisher.publishEvent(new StockBajoMinimoEvent(
                    empresaId, productoId, bodegaId, stockActual, maxMin.getStockMinimo()));
        }
    }
}

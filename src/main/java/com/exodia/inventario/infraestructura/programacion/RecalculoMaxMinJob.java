package com.exodia.inventario.infraestructura.programacion;

import com.exodia.inventario.aplicacion.consulta.StockQueryService;
import com.exodia.inventario.domain.evento.StockBajoMinimoEvent;
import com.exodia.inventario.domain.modelo.extension.MaximoMinimo;
import com.exodia.inventario.repositorio.extension.MaximoMinimoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static com.exodia.inventario.util.InventarioConstantes.BATCH_SIZE;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecalculoMaxMinJob {

    private final MaximoMinimoRepository maximoMinimoRepository;
    private final StockQueryService stockQueryService;
    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void recalcularStockMaxMin() {
        log.info("Iniciando recalculo nocturno de maximos/minimos");

        List<MaximoMinimo> todos = maximoMinimoRepository.findAll();
        int alertas = 0;
        int procesados = 0;

        for (MaximoMinimo maxMin : todos) {
            if (!Boolean.TRUE.equals(maxMin.getActivo())) {
                continue;
            }

            Long empresaId = maxMin.getEmpresa().getId();
            Long productoId = maxMin.getProductoId();
            Long bodegaId = maxMin.getBodega().getId();

            BigDecimal stockActual = stockQueryService
                    .obtenerStockPorProductoYBodega(empresaId, productoId, bodegaId);

            maxMin.setStockActualCalculado(stockActual);
            maxMin.setUltimaVerificacion(OffsetDateTime.now());

            if (stockActual.compareTo(maxMin.getStockMinimo()) < 0) {
                eventPublisher.publishEvent(new StockBajoMinimoEvent(
                        empresaId, productoId, bodegaId, stockActual, maxMin.getStockMinimo()));
                alertas++;
            }

            procesados++;

            if (procesados % BATCH_SIZE == 0) {
                maximoMinimoRepository.flush();
            }
        }

        maximoMinimoRepository.saveAll(todos);

        log.info("Recalculo nocturno completado: {} procesados, {} alertas de stock bajo", procesados, alertas);
    }
}

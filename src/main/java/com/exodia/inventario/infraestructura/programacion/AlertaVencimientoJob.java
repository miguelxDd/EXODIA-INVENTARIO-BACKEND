package com.exodia.inventario.infraestructura.programacion;

import com.exodia.inventario.domain.enums.EstadoContenedorCodigo;
import com.exodia.inventario.domain.evento.ContenedorPorVencerEvent;
import com.exodia.inventario.domain.modelo.contenedor.Contenedor;
import com.exodia.inventario.repositorio.contenedor.ContenedorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.exodia.inventario.util.InventarioConstantes.DIAS_ALERTA_VENCIMIENTO;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlertaVencimientoJob {

    private final ContenedorRepository contenedorRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(cron = "0 0 6 * * ?")
    @Transactional(readOnly = true)
    public void verificarVencimientos() {
        log.info("Iniciando verificacion de vencimientos");

        LocalDate limite = LocalDate.now().plusDays(DIAS_ALERTA_VENCIMIENTO);

        Specification<Contenedor> spec = (root, query, cb) -> cb.and(
                cb.isNotNull(root.get("fechaVencimiento")),
                cb.lessThanOrEqualTo(root.get("fechaVencimiento"), limite),
                cb.greaterThanOrEqualTo(root.get("fechaVencimiento"), LocalDate.now()),
                cb.equal(root.get("estado").get("codigo"),
                        EstadoContenedorCodigo.DISPONIBLE.getCodigo()),
                cb.equal(root.get("activo"), true)
        );

        List<Contenedor> porVencer = contenedorRepository.findAll(spec);

        int alertas = 0;
        for (Contenedor c : porVencer) {
            long diasRestantes = ChronoUnit.DAYS.between(LocalDate.now(), c.getFechaVencimiento());

            eventPublisher.publishEvent(new ContenedorPorVencerEvent(
                    c.getId(), c.getEmpresa().getId(), c.getProductoId(),
                    c.getFechaVencimiento(), diasRestantes));
            alertas++;
        }

        log.info("Verificacion de vencimientos completada: {} alertas generadas", alertas);
    }
}

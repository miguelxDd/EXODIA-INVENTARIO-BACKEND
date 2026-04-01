package com.exodia.inventario.infraestructura.programacion;

import com.exodia.inventario.aplicacion.comando.ConfiguracionEmpresaService;
import com.exodia.inventario.domain.enums.EstadoContenedorCodigo;
import com.exodia.inventario.domain.evento.ContenedorPorVencerEvent;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;
import com.exodia.inventario.domain.modelo.contenedor.Contenedor;
import com.exodia.inventario.domain.modelo.extension.ConfiguracionEmpresa;
import com.exodia.inventario.repositorio.catalogo.EmpresaRepository;
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

@Component
@RequiredArgsConstructor
@Slf4j
public class AlertaVencimientoJob {

    private final ContenedorRepository contenedorRepository;
    private final EmpresaRepository empresaRepository;
    private final ConfiguracionEmpresaService configuracionEmpresaService;
    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(cron = "0 0 6 * * ?")
    @Transactional
    public void verificarVencimientos() {
        log.info("Iniciando verificacion de vencimientos");

        int totalAlertas = 0;

        List<Empresa> empresasActivas = empresaRepository.findAll().stream()
                .filter(e -> Boolean.TRUE.equals(e.getActivo()))
                .toList();

        for (Empresa empresa : empresasActivas) {
            totalAlertas += verificarVencimientosEmpresa(empresa);
        }

        log.info("Verificacion de vencimientos completada: {} alertas generadas", totalAlertas);
    }

    private int verificarVencimientosEmpresa(Empresa empresa) {
        ConfiguracionEmpresa config = configuracionEmpresaService.obtenerEntidadOCrear(empresa.getId());
        LocalDate limite = LocalDate.now().plusDays(config.getDiasAlertaVencimiento());

        Specification<Contenedor> spec = (root, query, cb) -> cb.and(
                cb.equal(root.get("empresa").get("id"), empresa.getId()),
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
                    c.getId(), empresa.getId(), c.getProductoId(),
                    c.getFechaVencimiento(), diasRestantes));
            alertas++;
        }

        if (alertas > 0) {
            log.info("Empresa {}: {} alertas de vencimiento (dias alerta: {})",
                    empresa.getCodigo(), alertas, config.getDiasAlertaVencimiento());
        }

        return alertas;
    }
}

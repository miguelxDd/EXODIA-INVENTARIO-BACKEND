package com.exodia.inventario.infraestructura.programacion;

import com.exodia.inventario.domain.modelo.contenedor.Reserva;
import com.exodia.inventario.repositorio.contenedor.ReservaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

import static com.exodia.inventario.util.InventarioConstantes.ESTADO_RESERVA_EXPIRADA;
import static com.exodia.inventario.util.InventarioConstantes.ESTADO_RESERVA_PARCIAL;
import static com.exodia.inventario.util.InventarioConstantes.ESTADO_RESERVA_PENDIENTE;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExpiracionReservasJob {

    private final ReservaRepository reservaRepository;

    @Scheduled(cron = "0 0 * * * ?")
    @Transactional
    public void expirarReservasVencidas() {
        log.info("Iniciando expiracion de reservas vencidas");

        List<Reserva> expiradas = reservaRepository.findByEstadoInAndFechaExpiracionBefore(
                List.of(ESTADO_RESERVA_PENDIENTE, ESTADO_RESERVA_PARCIAL),
                OffsetDateTime.now());

        for (Reserva reserva : expiradas) {
            reserva.setEstado(ESTADO_RESERVA_EXPIRADA);
        }

        if (!expiradas.isEmpty()) {
            reservaRepository.saveAll(expiradas);
            log.info("Reservas expiradas: {}", expiradas.size());
        }
    }
}

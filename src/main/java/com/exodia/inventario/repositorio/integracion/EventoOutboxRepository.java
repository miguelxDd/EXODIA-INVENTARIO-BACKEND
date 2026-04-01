package com.exodia.inventario.repositorio.integracion;

import com.exodia.inventario.domain.enums.EstadoOutbox;
import com.exodia.inventario.domain.modelo.integracion.EventoOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventoOutboxRepository extends JpaRepository<EventoOutbox, Long> {

    List<EventoOutbox> findTop100ByEstadoOrderByCreadoEnAsc(EstadoOutbox estado);
}

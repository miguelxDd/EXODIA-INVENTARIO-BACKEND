package com.exodia.inventario.repositorio.contenedor;

import com.exodia.inventario.domain.modelo.contenedor.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByContenedorIdAndEstadoIn(Long contenedorId, List<String> estados);

    List<Reserva> findByContenedorIdAndEmpresaIdAndEstadoIn(Long contenedorId, Long empresaId, List<String> estados);

    List<Reserva> findByEstadoInAndFechaExpiracionBefore(List<String> estados, OffsetDateTime fecha);

    @Query("""
            SELECT COALESCE(SUM(r.cantidadReservada - r.cantidadCumplida), 0)
            FROM Reserva r
            WHERE r.contenedor.id = :contenedorId AND r.estado IN ('PENDIENTE','PARCIAL')
            """)
    BigDecimal obtenerCantidadReservada(@Param("contenedorId") Long contenedorId);
}

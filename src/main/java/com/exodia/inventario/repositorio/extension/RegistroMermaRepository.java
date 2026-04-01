package com.exodia.inventario.repositorio.extension;

import com.exodia.inventario.domain.modelo.extension.RegistroMerma;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public interface RegistroMermaRepository extends JpaRepository<RegistroMerma, Long> {

    Page<RegistroMerma> findByEmpresaId(Long empresaId, Pageable pageable);

    @Query("SELECT r FROM RegistroMerma r WHERE r.empresa.id = :empresaId "
            + "AND r.contenedor.productoId = :productoId "
            + "AND (:bodegaId IS NULL OR r.contenedor.bodega.id = :bodegaId) "
            + "AND r.creadoEn >= :desde ORDER BY r.creadoEn DESC")
    Optional<RegistroMerma> findUltimaMermaDentroDeVentana(Long empresaId, Long productoId,
                                                            Long bodegaId, OffsetDateTime desde);
}

package com.exodia.inventario.repositorio.contenedor;

import com.exodia.inventario.domain.modelo.contenedor.Contenedor;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContenedorRepository extends JpaRepository<Contenedor, Long>,
        JpaSpecificationExecutor<Contenedor> {

    Optional<Contenedor> findByEmpresaIdAndCodigoBarras(Long empresaId, String codigoBarras);

    boolean existsByEmpresaIdAndCodigoBarras(Long empresaId, String codigoBarras);

    @Query("SELECT c FROM Contenedor c WHERE c.id = :id")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Contenedor> findByIdForUpdate(@Param("id") Long id);
}

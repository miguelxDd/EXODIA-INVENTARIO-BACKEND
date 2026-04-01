package com.exodia.inventario.repositorio.extension;

import com.exodia.inventario.domain.modelo.extension.FotoCosto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FotoCostoRepository extends JpaRepository<FotoCosto, Long> {

    Page<FotoCosto> findByEmpresaIdOrderByFechaFotoDesc(Long empresaId, Pageable pageable);
}

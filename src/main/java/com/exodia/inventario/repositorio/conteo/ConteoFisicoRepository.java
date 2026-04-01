package com.exodia.inventario.repositorio.conteo;

import com.exodia.inventario.domain.modelo.conteo.ConteoFisico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConteoFisicoRepository extends JpaRepository<ConteoFisico, Long> {

    Page<ConteoFisico> findByEmpresaIdOrderByCreadoEnDesc(Long empresaId, Pageable pageable);
}

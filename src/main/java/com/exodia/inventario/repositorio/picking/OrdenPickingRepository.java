package com.exodia.inventario.repositorio.picking;

import com.exodia.inventario.domain.modelo.picking.OrdenPicking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdenPickingRepository extends JpaRepository<OrdenPicking, Long> {

    Page<OrdenPicking> findByEmpresaIdOrderByCreadoEnDesc(Long empresaId, Pageable pageable);
}

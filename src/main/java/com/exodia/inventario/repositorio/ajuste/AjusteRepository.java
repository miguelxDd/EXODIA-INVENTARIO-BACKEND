package com.exodia.inventario.repositorio.ajuste;

import com.exodia.inventario.domain.modelo.ajuste.Ajuste;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AjusteRepository extends JpaRepository<Ajuste, Long> {

    Page<Ajuste> findByEmpresaIdOrderByCreadoEnDesc(Long empresaId, Pageable pageable);
}

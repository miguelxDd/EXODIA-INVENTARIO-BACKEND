package com.exodia.inventario.repositorio.recepcion;

import com.exodia.inventario.domain.modelo.recepcion.Recepcion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecepcionRepository extends JpaRepository<Recepcion, Long> {

    Page<Recepcion> findByEmpresaIdOrderByCreadoEnDesc(Long empresaId, Pageable pageable);
}

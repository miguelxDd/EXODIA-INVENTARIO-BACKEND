package com.exodia.inventario.repositorio.extension;

import com.exodia.inventario.domain.modelo.extension.Auditoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    Page<Auditoria> findByEmpresaIdOrderByCreadoEnDesc(Long empresaId, Pageable pageable);

    List<Auditoria> findByEntidadAndEntidadId(String entidad, Long entidadId);
}

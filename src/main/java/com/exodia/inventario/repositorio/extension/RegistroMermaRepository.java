package com.exodia.inventario.repositorio.extension;

import com.exodia.inventario.domain.modelo.extension.RegistroMerma;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistroMermaRepository extends JpaRepository<RegistroMerma, Long> {

    Page<RegistroMerma> findByEmpresaId(Long empresaId, Pageable pageable);
}

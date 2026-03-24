package com.exodia.inventario.repositorio.transferencia;

import com.exodia.inventario.domain.modelo.transferencia.Transferencia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferenciaRepository extends JpaRepository<Transferencia, Long> {

    Page<Transferencia> findByEmpresaIdOrderByCreadoEnDesc(Long empresaId, Pageable pageable);
}

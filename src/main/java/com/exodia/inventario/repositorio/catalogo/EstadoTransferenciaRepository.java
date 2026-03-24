package com.exodia.inventario.repositorio.catalogo;

import com.exodia.inventario.domain.modelo.catalogo.EstadoTransferencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstadoTransferenciaRepository extends JpaRepository<EstadoTransferencia, Long> {

    Optional<EstadoTransferencia> findByCodigo(String codigo);
}

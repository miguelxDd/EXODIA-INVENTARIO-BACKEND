package com.exodia.inventario.repositorio.catalogo;

import com.exodia.inventario.domain.modelo.catalogo.Bodega;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BodegaRepository extends JpaRepository<Bodega, Long> {

    List<Bodega> findByEmpresaIdAndActivoTrue(Long empresaId);

    Optional<Bodega> findByEmpresaIdAndCodigo(Long empresaId, String codigo);
}

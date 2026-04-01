package com.exodia.inventario.repositorio.catalogo;

import com.exodia.inventario.domain.modelo.catalogo.Unidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UnidadRepository extends JpaRepository<Unidad, Long> {

    List<Unidad> findByEmpresaIdAndActivoTrue(Long empresaId);

    Optional<Unidad> findByEmpresaIdAndCodigo(Long empresaId, String codigo);
}

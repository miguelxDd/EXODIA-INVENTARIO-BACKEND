package com.exodia.inventario.repositorio.extension;

import com.exodia.inventario.domain.modelo.extension.ConfigMerma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConfigMermaRepository extends JpaRepository<ConfigMerma, Long> {

    List<ConfigMerma> findByEmpresaIdAndActivoTrue(Long empresaId);

    Optional<ConfigMerma> findByEmpresaIdAndProductoIdAndBodegaIdAndActivoTrue(
            Long empresaId, Long productoId, Long bodegaId);

    Optional<ConfigMerma> findFirstByEmpresaIdAndProductoIdAndBodegaIdAndActivoTrueOrEmpresaIdAndProductoIdAndBodegaIdIsNullAndActivoTrue(
            Long empresaId1, Long productoId1, Long bodegaId,
            Long empresaId2, Long productoId2);

    Optional<ConfigMerma> findFirstByEmpresaIdAndActivoTrueAndProductoIdAndBodegaId(
            Long empresaId, Long productoId, Long bodegaId);
}

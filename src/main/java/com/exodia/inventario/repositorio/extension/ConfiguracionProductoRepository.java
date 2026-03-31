package com.exodia.inventario.repositorio.extension;

import com.exodia.inventario.domain.modelo.extension.ConfiguracionProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConfiguracionProductoRepository extends JpaRepository<ConfiguracionProducto, Long> {

    Optional<ConfiguracionProducto> findByEmpresaIdAndProductoId(Long empresaId, Long productoId);

    List<ConfiguracionProducto> findByEmpresaIdAndActivoTrue(Long empresaId);
}

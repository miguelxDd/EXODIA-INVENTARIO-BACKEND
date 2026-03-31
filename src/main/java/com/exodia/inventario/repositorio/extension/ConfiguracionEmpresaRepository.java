package com.exodia.inventario.repositorio.extension;

import com.exodia.inventario.domain.modelo.extension.ConfiguracionEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfiguracionEmpresaRepository extends JpaRepository<ConfiguracionEmpresa, Long> {

    Optional<ConfiguracionEmpresa> findByEmpresaId(Long empresaId);
}

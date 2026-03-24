package com.exodia.inventario.repositorio.extension;

import com.exodia.inventario.domain.modelo.extension.ConfigMerma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfigMermaRepository extends JpaRepository<ConfigMerma, Long> {

    List<ConfigMerma> findByEmpresaIdAndActivoTrue(Long empresaId);
}

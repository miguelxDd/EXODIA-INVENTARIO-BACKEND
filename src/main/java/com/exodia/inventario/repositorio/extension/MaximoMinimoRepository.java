package com.exodia.inventario.repositorio.extension;

import com.exodia.inventario.domain.modelo.extension.MaximoMinimo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaximoMinimoRepository extends JpaRepository<MaximoMinimo, Long> {

    Optional<MaximoMinimo> findByEmpresaIdAndProductoIdAndBodegaId(Long empresaId,
                                                                    Long productoId,
                                                                    Long bodegaId);

    List<MaximoMinimo> findByEmpresaIdAndBodegaId(Long empresaId, Long bodegaId);
}

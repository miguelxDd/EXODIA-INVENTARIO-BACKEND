package com.exodia.inventario.repositorio.contenedor;

import com.exodia.inventario.domain.modelo.contenedor.Lote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoteRepository extends JpaRepository<Lote, Long> {

    Optional<Lote> findByEmpresaIdAndNumeroLoteAndProductoId(Long empresaId, String numeroLote,
                                                              Long productoId);

    List<Lote> findByEmpresaIdAndProductoId(Long empresaId, Long productoId);
}

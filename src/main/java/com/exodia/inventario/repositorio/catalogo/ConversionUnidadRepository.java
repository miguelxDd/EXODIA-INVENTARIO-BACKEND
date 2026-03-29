package com.exodia.inventario.repositorio.catalogo;

import com.exodia.inventario.domain.modelo.catalogo.ConversionUnidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversionUnidadRepository extends JpaRepository<ConversionUnidad, Long> {

    List<ConversionUnidad> findByEmpresaIdAndActivoTrue(Long empresaId);

    Optional<ConversionUnidad> findByEmpresaIdAndUnidadOrigenIdAndUnidadDestinoIdAndProductoId(
            Long empresaId, Long unidadOrigenId, Long unidadDestinoId, Long productoId);

    Optional<ConversionUnidad> findByEmpresaIdAndUnidadOrigenIdAndUnidadDestinoIdAndProductoIdIsNull(
            Long empresaId, Long unidadOrigenId, Long unidadDestinoId);
}

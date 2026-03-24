package com.exodia.inventario.repositorio.catalogo;

import com.exodia.inventario.domain.modelo.catalogo.TipoAjuste;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoAjusteRepository extends JpaRepository<TipoAjuste, Long> {

    Optional<TipoAjuste> findByCodigo(String codigo);
}

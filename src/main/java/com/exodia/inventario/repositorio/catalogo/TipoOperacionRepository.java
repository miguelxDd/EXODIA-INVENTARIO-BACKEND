package com.exodia.inventario.repositorio.catalogo;

import com.exodia.inventario.domain.modelo.catalogo.TipoOperacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoOperacionRepository extends JpaRepository<TipoOperacion, Long> {

    Optional<TipoOperacion> findByCodigo(String codigo);
}

package com.exodia.inventario.repositorio.catalogo;

import com.exodia.inventario.domain.modelo.catalogo.EstadoContenedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstadoContenedorRepository extends JpaRepository<EstadoContenedor, Long> {

    Optional<EstadoContenedor> findByCodigo(String codigo);
}

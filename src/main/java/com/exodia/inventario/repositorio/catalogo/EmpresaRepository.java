package com.exodia.inventario.repositorio.catalogo;

import com.exodia.inventario.domain.modelo.catalogo.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    Optional<Empresa> findByCodigo(String codigo);
}

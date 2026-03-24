package com.exodia.inventario.repositorio.extension;

import com.exodia.inventario.domain.modelo.extension.SecuenciaBarcode;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SecuenciaBarcodeRepository extends JpaRepository<SecuenciaBarcode, Long> {

    Optional<SecuenciaBarcode> findByEmpresaIdAndPrefijo(Long empresaId, String prefijo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SecuenciaBarcode s WHERE s.empresa.id = :empresaId AND s.prefijo = :prefijo")
    Optional<SecuenciaBarcode> findByEmpresaIdAndPrefijoForUpdate(@Param("empresaId") Long empresaId,
                                                                   @Param("prefijo") String prefijo);
}

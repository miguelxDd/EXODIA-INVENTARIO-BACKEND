package com.exodia.inventario.aplicacion.consulta.impl;

import com.exodia.inventario.aplicacion.consulta.KardexQueryService;
import com.exodia.inventario.domain.modelo.contenedor.Operacion;
import com.exodia.inventario.repositorio.contenedor.OperacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * Implementacion del servicio de consultas de kardex.
 * Todas las operaciones son de solo lectura.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KardexQueryServiceImpl implements KardexQueryService {

    private final OperacionRepository operacionRepository;

    @Override
    public Page<Operacion> consultarKardex(Long empresaId,
                                            Long contenedorId,
                                            String codigoBarras,
                                            Long productoId,
                                            Long bodegaId,
                                            OffsetDateTime fechaDesde,
                                            OffsetDateTime fechaHasta,
                                            Pageable pageable) {
        return operacionRepository.findKardex(
                empresaId, contenedorId, codigoBarras, productoId,
                bodegaId, fechaDesde, fechaHasta, pageable);
    }
}

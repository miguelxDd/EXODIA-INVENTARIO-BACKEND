package com.exodia.inventario.aplicacion.consulta;

import com.exodia.inventario.domain.modelo.contenedor.Operacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;

/**
 * Servicio de consultas de kardex (historial de operaciones).
 * Solo lectura, nunca modifica datos.
 */
public interface KardexQueryService {

    /**
     * Consulta el kardex con filtros opcionales y paginacion.
     *
     * @param empresaId    ID de la empresa (obligatorio)
     * @param contenedorId filtro por contenedor (opcional)
     * @param codigoBarras filtro por barcode (opcional)
     * @param productoId   filtro por producto (opcional)
     * @param bodegaId     filtro por bodega (opcional)
     * @param fechaDesde   filtro desde fecha (opcional)
     * @param fechaHasta   filtro hasta fecha (opcional)
     * @param pageable     paginacion y ordenamiento
     * @return pagina de operaciones del kardex
     */
    Page<Operacion> consultarKardex(Long empresaId,
                                     Long contenedorId,
                                     String codigoBarras,
                                     Long productoId,
                                     Long bodegaId,
                                     OffsetDateTime fechaDesde,
                                     OffsetDateTime fechaHasta,
                                     Pageable pageable);
}

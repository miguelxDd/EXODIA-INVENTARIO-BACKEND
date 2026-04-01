package com.exodia.inventario.repositorio.contenedor;

import com.exodia.inventario.domain.modelo.contenedor.Operacion;
import com.exodia.inventario.repositorio.proyeccion.ContenedorStockProjection;
import com.exodia.inventario.repositorio.proyeccion.ProductoBodegaStockProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface OperacionRepository extends JpaRepository<Operacion, Long> {

    // ── Stock por contenedor (JPQL) ──────────────────────────────────────────────
    @Query("""
            SELECT COALESCE(SUM(o.cantidad), 0)
            FROM Operacion o
            WHERE o.contenedor.id = :contenedorId AND o.activo = true
            """)
    BigDecimal obtenerStockPorContenedor(@Param("contenedorId") Long contenedorId);

    // ── Stock por codigo de barras (JPQL) ────────────────────────────────────────
    @Query("""
            SELECT COALESCE(SUM(o.cantidad), 0)
            FROM Operacion o
            WHERE o.codigoBarras = :codigoBarras AND o.empresa.id = :empresaId AND o.activo = true
            """)
    BigDecimal obtenerStockPorBarcode(@Param("empresaId") Long empresaId,
                                      @Param("codigoBarras") String codigoBarras);

    // ── Stock por producto y bodega (JPQL) ───────────────────────────────────────
    @Query("""
            SELECT COALESCE(SUM(o.cantidad), 0)
            FROM Operacion o
            WHERE o.productoId = :productoId AND o.bodega.id = :bodegaId
              AND o.empresa.id = :empresaId AND o.activo = true
            """)
    BigDecimal obtenerStockPorProductoYBodega(@Param("empresaId") Long empresaId,
                                              @Param("productoId") Long productoId,
                                              @Param("bodegaId") Long bodegaId);

    // ── Stock consolidado (native, paginado) ─────────────────────────────────────
    @Query(value = """
            SELECT
                c.id AS contenedorId, c.codigo_barras AS codigoBarras,
                c.producto_id AS productoId, c.proveedor_id AS proveedorId,
                c.unidad_id AS unidadId, c.bodega_id AS bodegaId,
                c.ubicacion_id AS ubicacionId, c.precio_unitario AS precioUnitario,
                c.numero_lote AS numeroLote, c.fecha_vencimiento AS fechaVencimiento,
                ec.codigo AS estadoCodigo,
                COALESCE(SUM(o.cantidad) FILTER (WHERE o.activo), 0) AS stockCantidad,
                COALESCE((
                    SELECT SUM(r.cantidad_reservada - r.cantidad_cumplida)
                    FROM inv_reservas r
                    WHERE r.contenedor_id = c.id AND r.estado IN ('PENDIENTE','PARCIAL')
                ), 0) AS cantidadReservada
            FROM inv_contenedores c
            JOIN inv_estados_contenedor ec ON ec.id = c.estado_id
            LEFT JOIN inv_operaciones o ON o.contenedor_id = c.id AND o.activo = true
            WHERE c.empresa_id = :empresaId AND c.activo = true
              AND (:bodegaId IS NULL OR c.bodega_id = :bodegaId)
              AND (:productoId IS NULL OR c.producto_id = :productoId)
              AND (:proveedorId IS NULL OR c.proveedor_id = :proveedorId)
              AND (:codigoBarras IS NULL OR c.codigo_barras = :codigoBarras)
              AND (:numeroLote IS NULL OR c.numero_lote = :numeroLote)
            GROUP BY c.id, c.codigo_barras, c.producto_id, c.proveedor_id, c.unidad_id,
                     c.bodega_id, c.ubicacion_id, c.precio_unitario, c.numero_lote,
                     c.fecha_vencimiento, ec.codigo
            HAVING COALESCE(SUM(o.cantidad) FILTER (WHERE o.activo), 0) > 0
            """, nativeQuery = true)
    Page<ContenedorStockProjection> findConsolidatedStock(
            @Param("empresaId") Long empresaId,
            @Param("bodegaId") Long bodegaId,
            @Param("productoId") Long productoId,
            @Param("proveedorId") Long proveedorId,
            @Param("codigoBarras") String codigoBarras,
            @Param("numeroLote") String numeroLote,
            Pageable pageable);

    // ── Stock agrupado por producto + bodega (native) ────────────────────────────
    @Query(value = """
            SELECT o.producto_id AS productoId, o.bodega_id AS bodegaId,
                   o.unidad_id AS unidadId, SUM(o.cantidad) AS stockCantidad
            FROM inv_operaciones o
            WHERE o.empresa_id = :empresaId AND o.activo = true
              AND (:bodegaId IS NULL OR o.bodega_id = :bodegaId)
              AND (:productoId IS NULL OR o.producto_id = :productoId)
            GROUP BY o.producto_id, o.bodega_id, o.unidad_id
            HAVING SUM(o.cantidad) <> 0
            """, nativeQuery = true)
    List<ProductoBodegaStockProjection> findStockPorProductoYBodega(
            @Param("empresaId") Long empresaId,
            @Param("bodegaId") Long bodegaId,
            @Param("productoId") Long productoId);

    // ── Kardex (JPQL, paginado) ──────────────────────────────────────────────────
    @Query("""
            SELECT o FROM Operacion o
            WHERE o.empresa.id = :empresaId AND o.activo = true
              AND (:contenedorId IS NULL OR o.contenedor.id = :contenedorId)
              AND (:codigoBarras IS NULL OR o.codigoBarras = :codigoBarras)
              AND (:productoId IS NULL OR o.productoId = :productoId)
              AND (:bodegaId IS NULL OR o.bodega.id = :bodegaId)
              AND (:fechaDesde IS NULL OR o.fechaOperacion >= :fechaDesde)
              AND (:fechaHasta IS NULL OR o.fechaOperacion <= :fechaHasta)
            ORDER BY o.fechaOperacion DESC, o.id DESC
            """)
    Page<Operacion> findKardex(
            @Param("empresaId") Long empresaId,
            @Param("contenedorId") Long contenedorId,
            @Param("codigoBarras") String codigoBarras,
            @Param("productoId") Long productoId,
            @Param("bodegaId") Long bodegaId,
            @Param("fechaDesde") OffsetDateTime fechaDesde,
            @Param("fechaHasta") OffsetDateTime fechaHasta,
            Pageable pageable);

    @Query("""
            SELECT o FROM Operacion o
            WHERE o.empresa.id = :empresaId AND o.activo = true
              AND o.productoId = :productoId
              AND (:bodegaId IS NULL OR o.bodega.id = :bodegaId)
              AND (:fechaDesde IS NULL OR o.fechaOperacion >= :fechaDesde)
              AND (:fechaHasta IS NULL OR o.fechaOperacion <= :fechaHasta)
            ORDER BY o.fechaOperacion ASC, o.id ASC
            """)
    List<Operacion> findAuxiliarInventario(
            @Param("empresaId") Long empresaId,
            @Param("productoId") Long productoId,
            @Param("bodegaId") Long bodegaId,
            @Param("fechaDesde") OffsetDateTime fechaDesde,
            @Param("fechaHasta") OffsetDateTime fechaHasta);

    @Query("""
            SELECT COALESCE(SUM(o.cantidad), 0)
            FROM Operacion o
            WHERE o.empresa.id = :empresaId AND o.activo = true
              AND o.productoId = :productoId
              AND (:bodegaId IS NULL OR o.bodega.id = :bodegaId)
              AND (:fechaDesde IS NULL OR o.fechaOperacion < :fechaDesde)
            """)
    BigDecimal obtenerAcumuladoCantidadAntesDe(
            @Param("empresaId") Long empresaId,
            @Param("productoId") Long productoId,
            @Param("bodegaId") Long bodegaId,
            @Param("fechaDesde") OffsetDateTime fechaDesde);

    @Query("""
            SELECT COALESCE(SUM(o.cantidad * o.precioUnitario), 0)
            FROM Operacion o
            WHERE o.empresa.id = :empresaId AND o.activo = true
              AND o.productoId = :productoId
              AND (:bodegaId IS NULL OR o.bodega.id = :bodegaId)
              AND (:fechaDesde IS NULL OR o.fechaOperacion < :fechaDesde)
            """)
    BigDecimal obtenerAcumuladoValorAntesDe(
            @Param("empresaId") Long empresaId,
            @Param("productoId") Long productoId,
            @Param("bodegaId") Long bodegaId,
            @Param("fechaDesde") OffsetDateTime fechaDesde);

    // ── Costo promedio ponderado por producto + bodega (native) ───────────────────
    @Query(value = """
            SELECT COALESCE(
                SUM(sub.precio_unitario * sub.stock) / NULLIF(SUM(sub.stock), 0),
                0
            )
            FROM (
                SELECT c.precio_unitario,
                       COALESCE(SUM(o.cantidad), 0) AS stock
                FROM inv_contenedores c
                LEFT JOIN inv_operaciones o ON o.contenedor_id = c.id AND o.activo = true
                WHERE c.empresa_id = :empresaId
                  AND c.producto_id = :productoId
                  AND c.bodega_id = :bodegaId
                  AND c.activo = true
                GROUP BY c.id, c.precio_unitario
                HAVING COALESCE(SUM(o.cantidad), 0) > 0
            ) sub
            """, nativeQuery = true)
    BigDecimal obtenerCostoPromedioPonderado(@Param("empresaId") Long empresaId,
                                             @Param("productoId") Long productoId,
                                             @Param("bodegaId") Long bodegaId);

    // ── Contenedores disponibles FEFO (native) ───────────────────────────────────
    @Query(value = """
            SELECT
                c.id AS contenedorId, c.codigo_barras AS codigoBarras,
                c.producto_id AS productoId, c.proveedor_id AS proveedorId,
                c.unidad_id AS unidadId, c.bodega_id AS bodegaId,
                c.ubicacion_id AS ubicacionId, c.precio_unitario AS precioUnitario,
                c.numero_lote AS numeroLote, c.fecha_vencimiento AS fechaVencimiento,
                ec.codigo AS estadoCodigo,
                COALESCE(SUM(o.cantidad), 0) AS stockCantidad,
                COALESCE((
                    SELECT SUM(r.cantidad_reservada - r.cantidad_cumplida)
                    FROM inv_reservas r
                    WHERE r.contenedor_id = c.id AND r.estado IN ('PENDIENTE','PARCIAL')
                ), 0) AS cantidadReservada
            FROM inv_contenedores c
            JOIN inv_estados_contenedor ec ON ec.id = c.estado_id
            LEFT JOIN inv_operaciones o ON o.contenedor_id = c.id AND o.activo = true
            WHERE c.empresa_id = :empresaId AND c.producto_id = :productoId
              AND c.bodega_id = :bodegaId AND ec.permite_picking = true AND c.activo = true
            GROUP BY c.id, c.codigo_barras, c.producto_id, c.proveedor_id, c.unidad_id,
                     c.bodega_id, c.ubicacion_id, c.precio_unitario, c.numero_lote,
                     c.fecha_vencimiento, ec.codigo
            HAVING COALESCE(SUM(o.cantidad), 0)
                   - COALESCE((SELECT SUM(r.cantidad_reservada - r.cantidad_cumplida)
                               FROM inv_reservas r WHERE r.contenedor_id = c.id
                               AND r.estado IN ('PENDIENTE','PARCIAL')), 0) > 0
            -- cantidadDisponible = stockCantidad - cantidadReservada (calculada en ContenedorStockProjection)
            ORDER BY c.fecha_vencimiento ASC NULLS LAST, c.creado_en ASC
            """, nativeQuery = true)
    List<ContenedorStockProjection> findContenedoresDisponiblesFEFO(
            @Param("empresaId") Long empresaId,
            @Param("productoId") Long productoId,
            @Param("bodegaId") Long bodegaId);

    // ── Contenedores proximos a vencer (native) ──────────────────────────────────
    @Query(value = """
            SELECT
                c.id AS contenedorId, c.codigo_barras AS codigoBarras,
                c.producto_id AS productoId, c.proveedor_id AS proveedorId,
                c.unidad_id AS unidadId, c.bodega_id AS bodegaId,
                c.ubicacion_id AS ubicacionId, c.precio_unitario AS precioUnitario,
                c.numero_lote AS numeroLote, c.fecha_vencimiento AS fechaVencimiento,
                ec.codigo AS estadoCodigo,
                COALESCE(SUM(o.cantidad), 0) AS stockCantidad,
                COALESCE((
                    SELECT SUM(r.cantidad_reservada - r.cantidad_cumplida)
                    FROM inv_reservas r
                    WHERE r.contenedor_id = c.id AND r.estado IN ('PENDIENTE','PARCIAL')
                ), 0) AS cantidadReservada
            FROM inv_contenedores c
            JOIN inv_estados_contenedor ec ON ec.id = c.estado_id
            LEFT JOIN inv_operaciones o ON o.contenedor_id = c.id AND o.activo = true
            WHERE c.empresa_id = :empresaId AND c.activo = true
              AND c.fecha_vencimiento IS NOT NULL
              AND c.fecha_vencimiento <= CURRENT_DATE + CAST(:diasAlerta AS INTEGER)
              AND (:bodegaId IS NULL OR c.bodega_id = :bodegaId)
            GROUP BY c.id, c.codigo_barras, c.producto_id, c.proveedor_id, c.unidad_id,
                     c.bodega_id, c.ubicacion_id, c.precio_unitario, c.numero_lote,
                     c.fecha_vencimiento, ec.codigo
            HAVING COALESCE(SUM(o.cantidad), 0) > 0
            ORDER BY c.fecha_vencimiento ASC
            """, nativeQuery = true)
    List<ContenedorStockProjection> findContenedoresProximosAVencer(
            @Param("empresaId") Long empresaId,
            @Param("bodegaId") Long bodegaId,
            @Param("diasAlerta") int diasAlerta);

    // ── Contenedores disponibles FIFO (native) ───────────────────────────────────
    @Query(value = """
            SELECT
                c.id AS contenedorId, c.codigo_barras AS codigoBarras,
                c.producto_id AS productoId, c.proveedor_id AS proveedorId,
                c.unidad_id AS unidadId, c.bodega_id AS bodegaId,
                c.ubicacion_id AS ubicacionId, c.precio_unitario AS precioUnitario,
                c.numero_lote AS numeroLote, c.fecha_vencimiento AS fechaVencimiento,
                ec.codigo AS estadoCodigo,
                COALESCE(SUM(o.cantidad), 0) AS stockCantidad,
                COALESCE((
                    SELECT SUM(r.cantidad_reservada - r.cantidad_cumplida)
                    FROM inv_reservas r
                    WHERE r.contenedor_id = c.id AND r.estado IN ('PENDIENTE','PARCIAL')
                ), 0) AS cantidadReservada
            FROM inv_contenedores c
            JOIN inv_estados_contenedor ec ON ec.id = c.estado_id
            LEFT JOIN inv_operaciones o ON o.contenedor_id = c.id AND o.activo = true
            WHERE c.empresa_id = :empresaId AND c.producto_id = :productoId
              AND c.bodega_id = :bodegaId AND ec.permite_picking = true AND c.activo = true
            GROUP BY c.id, c.codigo_barras, c.producto_id, c.proveedor_id, c.unidad_id,
                     c.bodega_id, c.ubicacion_id, c.precio_unitario, c.numero_lote,
                     c.fecha_vencimiento, ec.codigo
            HAVING COALESCE(SUM(o.cantidad), 0)
                   - COALESCE((SELECT SUM(r.cantidad_reservada - r.cantidad_cumplida)
                               FROM inv_reservas r WHERE r.contenedor_id = c.id
                               AND r.estado IN ('PENDIENTE','PARCIAL')), 0) > 0
            ORDER BY c.creado_en ASC
            """, nativeQuery = true)
    List<ContenedorStockProjection> findContenedoresDisponiblesFIFO(
            @Param("empresaId") Long empresaId,
            @Param("productoId") Long productoId,
            @Param("bodegaId") Long bodegaId);
}

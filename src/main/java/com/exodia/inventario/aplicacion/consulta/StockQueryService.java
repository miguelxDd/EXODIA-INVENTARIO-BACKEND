package com.exodia.inventario.aplicacion.consulta;

import com.exodia.inventario.repositorio.proyeccion.ContenedorStockProjection;
import com.exodia.inventario.repositorio.proyeccion.ProductoBodegaStockProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

/**
 * Servicio de consultas de stock. Solo lectura, nunca modifica datos.
 * Usa queries nativos optimizados del repositorio de operaciones.
 */
public interface StockQueryService {

    /**
     * Obtiene el stock total de un contenedor por su ID.
     *
     * @param contenedorId ID del contenedor
     * @return stock total (SUM de operaciones activas)
     */
    BigDecimal obtenerStockContenedor(Long contenedorId);

    /**
     * Obtiene el stock total de un contenedor validando que pertenece a la empresa.
     *
     * @param empresaId    ID de la empresa
     * @param contenedorId ID del contenedor
     * @return stock total
     * @throws com.exodia.inventario.excepcion.EntidadNoEncontradaException si no pertenece a la empresa
     */
    BigDecimal obtenerStockContenedorPorEmpresa(Long empresaId, Long contenedorId);

    /**
     * Obtiene el stock total por codigo de barras.
     *
     * @param empresaId    ID de la empresa
     * @param codigoBarras codigo de barras del contenedor
     * @return stock total
     */
    BigDecimal obtenerStockPorBarcode(Long empresaId, String codigoBarras);

    /**
     * Obtiene el stock total de un producto en una bodega.
     *
     * @param empresaId  ID de la empresa
     * @param productoId ID del producto
     * @param bodegaId   ID de la bodega
     * @return stock total agregado
     */
    BigDecimal obtenerStockPorProductoYBodega(Long empresaId, Long productoId, Long bodegaId);

    /**
     * Consulta consolidada de stock con filtros opcionales, paginada.
     *
     * @param empresaId    ID de la empresa (obligatorio)
     * @param bodegaId     filtro por bodega (opcional)
     * @param productoId   filtro por producto (opcional)
     * @param proveedorId  filtro por proveedor (opcional)
     * @param codigoBarras filtro por barcode (opcional)
     * @param numeroLote   filtro por lote (opcional)
     * @param pageable     paginacion y ordenamiento
     * @return pagina de proyecciones de stock por contenedor
     */
    Page<ContenedorStockProjection> obtenerStockConsolidado(Long empresaId,
                                                            Long bodegaId,
                                                            Long productoId,
                                                            Long proveedorId,
                                                            String codigoBarras,
                                                            String numeroLote,
                                                            Pageable pageable);

    /**
     * Obtiene stock agrupado por producto y bodega.
     *
     * @param empresaId  ID de la empresa (obligatorio)
     * @param bodegaId   filtro por bodega (opcional)
     * @param productoId filtro por producto (opcional)
     * @return lista de proyecciones producto-bodega-stock
     */
    List<ProductoBodegaStockProjection> obtenerStockPorProductoBodega(Long empresaId,
                                                                      Long bodegaId,
                                                                      Long productoId);

    /**
     * Obtiene contenedores disponibles ordenados por FEFO para picking.
     *
     * @param empresaId  ID de la empresa
     * @param productoId ID del producto
     * @param bodegaId   ID de la bodega
     * @return contenedores con stock disponible, ordenados por vencimiento ASC
     */
    List<ContenedorStockProjection> obtenerContenedoresDisponiblesFEFO(Long empresaId,
                                                                       Long productoId,
                                                                       Long bodegaId);

    /**
     * Obtiene contenedores disponibles ordenados por FIFO para picking.
     *
     * @param empresaId  ID de la empresa
     * @param productoId ID del producto
     * @param bodegaId   ID de la bodega
     * @return contenedores con stock disponible, ordenados por fecha de creacion ASC
     */
    List<ContenedorStockProjection> obtenerContenedoresDisponiblesFIFO(Long empresaId,
                                                                       Long productoId,
                                                                       Long bodegaId);

    /**
     * Obtiene el costo promedio ponderado de un producto en una bodega.
     * Se calcula como SUM(precioUnitario * stock) / SUM(stock) sobre contenedores con stock > 0.
     *
     * @param empresaId  ID de la empresa
     * @param productoId ID del producto
     * @param bodegaId   ID de la bodega
     * @return costo promedio ponderado
     */
    BigDecimal obtenerCostoPromedioPonderado(Long empresaId, Long productoId, Long bodegaId);

    /**
     * Obtiene la cantidad reservada de un contenedor.
     *
     * @param contenedorId ID del contenedor
     * @return cantidad reservada pendiente
     */
    BigDecimal obtenerCantidadReservada(Long contenedorId);

    /**
     * Obtiene el stock disponible de un contenedor (total - reservado).
     *
     * @param contenedorId ID del contenedor
     * @return stock disponible
     */
    BigDecimal obtenerStockDisponible(Long contenedorId);
}

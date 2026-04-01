package com.exodia.inventario.aplicacion.consulta.impl;

import com.exodia.inventario.aplicacion.comando.ConfiguracionEmpresaService;
import com.exodia.inventario.aplicacion.consulta.StockQueryService;
import com.exodia.inventario.domain.modelo.extension.ConfiguracionEmpresa;
import com.exodia.inventario.domain.servicio.CalculadorStock;
import com.exodia.inventario.excepcion.EntidadNoEncontradaException;
import com.exodia.inventario.repositorio.contenedor.ContenedorRepository;
import com.exodia.inventario.repositorio.contenedor.OperacionRepository;
import com.exodia.inventario.repositorio.contenedor.ReservaRepository;
import com.exodia.inventario.repositorio.proyeccion.ContenedorStockProjection;
import com.exodia.inventario.repositorio.proyeccion.ProductoBodegaStockProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Implementacion del servicio de consultas de stock.
 * Todas las operaciones son de solo lectura.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockQueryServiceImpl implements StockQueryService {

    private final OperacionRepository operacionRepository;
    private final ContenedorRepository contenedorRepository;
    private final ReservaRepository reservaRepository;
    private final CalculadorStock calculadorStock;
    private final ConfiguracionEmpresaService configuracionEmpresaService;

    @Override
    public BigDecimal obtenerStockContenedor(Long contenedorId) {
        return operacionRepository.obtenerStockPorContenedor(contenedorId);
    }

    @Override
    public BigDecimal obtenerStockContenedorPorEmpresa(Long empresaId, Long contenedorId) {
        contenedorRepository.findById(contenedorId)
                .filter(c -> c.getEmpresa().getId().equals(empresaId))
                .orElseThrow(() -> new EntidadNoEncontradaException("Contenedor", contenedorId));
        return operacionRepository.obtenerStockPorContenedor(contenedorId);
    }

    @Override
    public BigDecimal obtenerStockPorBarcode(Long empresaId, String codigoBarras) {
        return operacionRepository.obtenerStockPorBarcode(empresaId, codigoBarras);
    }

    @Override
    public BigDecimal obtenerStockPorProductoYBodega(Long empresaId, Long productoId, Long bodegaId) {
        return operacionRepository.obtenerStockPorProductoYBodega(empresaId, productoId, bodegaId);
    }

    @Override
    public Page<ContenedorStockProjection> obtenerStockConsolidado(Long empresaId,
                                                                    Long bodegaId,
                                                                    Long productoId,
                                                                    Long proveedorId,
                                                                    String codigoBarras,
                                                                    String numeroLote,
                                                                    Pageable pageable) {
        return operacionRepository.findConsolidatedStock(
                empresaId, bodegaId, productoId, proveedorId, codigoBarras, numeroLote, pageable);
    }

    @Override
    public List<ProductoBodegaStockProjection> obtenerStockPorProductoBodega(Long empresaId,
                                                                             Long bodegaId,
                                                                             Long productoId) {
        return operacionRepository.findStockPorProductoYBodega(empresaId, bodegaId, productoId);
    }

    @Override
    public List<ContenedorStockProjection> obtenerContenedoresDisponiblesFEFO(Long empresaId,
                                                                              Long productoId,
                                                                              Long bodegaId) {
        return operacionRepository.findContenedoresDisponiblesFEFO(empresaId, productoId, bodegaId);
    }

    @Override
    public List<ContenedorStockProjection> obtenerContenedoresDisponiblesFIFO(Long empresaId,
                                                                              Long productoId,
                                                                              Long bodegaId) {
        return operacionRepository.findContenedoresDisponiblesFIFO(empresaId, productoId, bodegaId);
    }

    @Override
    public List<ContenedorStockProjection> obtenerContenedoresProximosAVencer(Long empresaId, Long bodegaId) {
        ConfiguracionEmpresa config = configuracionEmpresaService.obtenerEntidadOCrear(empresaId);
        return operacionRepository.findContenedoresProximosAVencer(
                empresaId, bodegaId, config.getDiasAlertaVencimiento());
    }

    @Override
    public BigDecimal obtenerCostoPromedioPonderado(Long empresaId, Long productoId, Long bodegaId) {
        return operacionRepository.obtenerCostoPromedioPonderado(empresaId, productoId, bodegaId);
    }

    @Override
    public BigDecimal obtenerCantidadReservada(Long contenedorId) {
        return reservaRepository.obtenerCantidadReservada(contenedorId);
    }

    @Override
    public BigDecimal obtenerStockDisponible(Long contenedorId) {
        BigDecimal stockTotal = obtenerStockContenedor(contenedorId);
        BigDecimal cantidadReservada = obtenerCantidadReservada(contenedorId);
        return calculadorStock.calcularStockDisponible(stockTotal, cantidadReservada);
    }
}

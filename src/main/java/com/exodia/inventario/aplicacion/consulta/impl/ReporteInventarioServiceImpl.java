package com.exodia.inventario.aplicacion.consulta.impl;

import com.exodia.inventario.aplicacion.consulta.ReporteInventarioService;
import com.exodia.inventario.aplicacion.consulta.StockQueryService;
import com.exodia.inventario.domain.modelo.contenedor.Operacion;
import com.exodia.inventario.excepcion.OperacionInvalidaException;
import com.exodia.inventario.interfaz.dto.respuesta.AuxiliarInventarioMovimientoResponse;
import com.exodia.inventario.interfaz.dto.respuesta.AuxiliarInventarioResponse;
import com.exodia.inventario.interfaz.dto.respuesta.ValorizacionActualResponse;
import com.exodia.inventario.repositorio.contenedor.OperacionRepository;
import com.exodia.inventario.repositorio.proyeccion.ProductoBodegaStockProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReporteInventarioServiceImpl implements ReporteInventarioService {

    private final OperacionRepository operacionRepository;
    private final StockQueryService stockQueryService;

    @Override
    public AuxiliarInventarioResponse generarAuxiliarInventario(Long empresaId,
                                                                Long productoId,
                                                                Long bodegaId,
                                                                OffsetDateTime fechaDesde,
                                                                OffsetDateTime fechaHasta) {
        validarRangoFechas(fechaDesde, fechaHasta);

        BigDecimal saldoInicialCantidad = fechaDesde != null
                ? operacionRepository.obtenerAcumuladoCantidadAntesDe(
                        empresaId, productoId, bodegaId, fechaDesde)
                : BigDecimal.ZERO;

        BigDecimal saldoInicialValor = fechaDesde != null
                ? operacionRepository.obtenerAcumuladoValorAntesDe(
                        empresaId, productoId, bodegaId, fechaDesde)
                : BigDecimal.ZERO;

        List<Operacion> operaciones = operacionRepository.findAuxiliarInventario(
                empresaId, productoId, bodegaId, fechaDesde, fechaHasta);

        BigDecimal saldoCantidad = saldoInicialCantidad;
        BigDecimal saldoValor = saldoInicialValor;
        BigDecimal totalEntradas = BigDecimal.ZERO;
        BigDecimal totalSalidas = BigDecimal.ZERO;
        List<AuxiliarInventarioMovimientoResponse> movimientos = new ArrayList<>();

        for (Operacion operacion : operaciones) {
            BigDecimal precioUnitario = valorSeguro(operacion.getPrecioUnitario());
            BigDecimal valorMovimiento = operacion.getCantidad().multiply(precioUnitario);
            saldoCantidad = saldoCantidad.add(operacion.getCantidad());
            saldoValor = saldoValor.add(valorMovimiento);

            if (operacion.getCantidad().compareTo(BigDecimal.ZERO) > 0) {
                totalEntradas = totalEntradas.add(operacion.getCantidad());
            } else if (operacion.getCantidad().compareTo(BigDecimal.ZERO) < 0) {
                totalSalidas = totalSalidas.add(operacion.getCantidad().abs());
            }

            movimientos.add(new AuxiliarInventarioMovimientoResponse(
                    operacion.getId(),
                    operacion.getFechaOperacion(),
                    operacion.getContenedor().getId(),
                    operacion.getCodigoBarras(),
                    operacion.getTipoOperacion().getCodigo(),
                    operacion.getTipoReferencia() != null ? operacion.getTipoReferencia().name() : null,
                    operacion.getReferenciaId(),
                    operacion.getCantidad(),
                    precioUnitario,
                    valorMovimiento,
                    saldoCantidad,
                    saldoValor,
                    operacion.getComentarios()));
        }

        return new AuxiliarInventarioResponse(
                empresaId,
                productoId,
                bodegaId,
                fechaDesde,
                fechaHasta,
                saldoInicialCantidad,
                saldoInicialValor,
                totalEntradas,
                totalSalidas,
                saldoCantidad,
                saldoValor,
                List.copyOf(movimientos));
    }

    @Override
    public String exportarAuxiliarInventarioCsv(Long empresaId,
                                                Long productoId,
                                                Long bodegaId,
                                                OffsetDateTime fechaDesde,
                                                OffsetDateTime fechaHasta) {
        AuxiliarInventarioResponse reporte = generarAuxiliarInventario(
                empresaId, productoId, bodegaId, fechaDesde, fechaHasta);

        StringBuilder csv = new StringBuilder();
        csv.append("empresaId,productoId,bodegaId,fechaDesde,fechaHasta,saldoInicialCantidad,saldoInicialValor,totalEntradas,totalSalidas,saldoFinalCantidad,saldoFinalValor\n");
        csv.append(csvValue(reporte.empresaId())).append(',')
                .append(csvValue(reporte.productoId())).append(',')
                .append(csvValue(reporte.bodegaId())).append(',')
                .append(csvValue(reporte.fechaDesde())).append(',')
                .append(csvValue(reporte.fechaHasta())).append(',')
                .append(csvValue(reporte.saldoInicialCantidad())).append(',')
                .append(csvValue(reporte.saldoInicialValor())).append(',')
                .append(csvValue(reporte.totalEntradas())).append(',')
                .append(csvValue(reporte.totalSalidas())).append(',')
                .append(csvValue(reporte.saldoFinalCantidad())).append(',')
                .append(csvValue(reporte.saldoFinalValor())).append('\n');

        csv.append('\n');
        csv.append("operacionId,fechaOperacion,contenedorId,codigoBarras,tipoOperacionCodigo,tipoReferencia,referenciaId,cantidad,precioUnitario,valorMovimiento,saldoCantidad,saldoValor,comentarios\n");

        reporte.movimientos().forEach(movimiento -> csv.append(csvValue(movimiento.operacionId())).append(',')
                .append(csvValue(movimiento.fechaOperacion())).append(',')
                .append(csvValue(movimiento.contenedorId())).append(',')
                .append(csvValue(movimiento.codigoBarras())).append(',')
                .append(csvValue(movimiento.tipoOperacionCodigo())).append(',')
                .append(csvValue(movimiento.tipoReferencia())).append(',')
                .append(csvValue(movimiento.referenciaId())).append(',')
                .append(csvValue(movimiento.cantidad())).append(',')
                .append(csvValue(movimiento.precioUnitario())).append(',')
                .append(csvValue(movimiento.valorMovimiento())).append(',')
                .append(csvValue(movimiento.saldoCantidad())).append(',')
                .append(csvValue(movimiento.saldoValor())).append(',')
                .append(csvValue(movimiento.comentarios())).append('\n'));

        return csv.toString();
    }

    @Override
    public List<ValorizacionActualResponse> obtenerValorizacionActual(Long empresaId,
                                                                      Long bodegaId,
                                                                      Long productoId) {
        List<ProductoBodegaStockProjection> stocks = stockQueryService
                .obtenerStockPorProductoBodega(empresaId, bodegaId, productoId);

        return stocks.stream()
                .filter(stock -> stock.getStockCantidad() != null
                        && stock.getStockCantidad().compareTo(BigDecimal.ZERO) > 0)
                .map(stock -> {
                    BigDecimal costoUnitario = stockQueryService.obtenerCostoPromedioPonderado(
                            empresaId, stock.getProductoId(), stock.getBodegaId());
                    BigDecimal costoTotal = stock.getStockCantidad().multiply(costoUnitario);
                    return new ValorizacionActualResponse(
                            stock.getProductoId(),
                            stock.getBodegaId(),
                            stock.getUnidadId(),
                            stock.getStockCantidad(),
                            costoUnitario,
                            costoTotal);
                })
                .sorted(Comparator
                        .comparing(ValorizacionActualResponse::bodegaId, Comparator.nullsLast(Long::compareTo))
                        .thenComparing(ValorizacionActualResponse::productoId, Comparator.nullsLast(Long::compareTo)))
                .toList();
    }

    @Override
    public String exportarValorizacionActualCsv(Long empresaId,
                                                Long bodegaId,
                                                Long productoId) {
        List<ValorizacionActualResponse> filas = obtenerValorizacionActual(empresaId, bodegaId, productoId);
        StringBuilder csv = new StringBuilder();
        csv.append("productoId,bodegaId,unidadId,cantidadStock,costoUnitario,costoTotal\n");
        filas.forEach(fila -> csv.append(csvValue(fila.productoId())).append(',')
                .append(csvValue(fila.bodegaId())).append(',')
                .append(csvValue(fila.unidadId())).append(',')
                .append(csvValue(fila.cantidadStock())).append(',')
                .append(csvValue(fila.costoUnitario())).append(',')
                .append(csvValue(fila.costoTotal())).append('\n'));
        return csv.toString();
    }

    private void validarRangoFechas(OffsetDateTime fechaDesde, OffsetDateTime fechaHasta) {
        if (fechaDesde != null && fechaHasta != null && fechaHasta.isBefore(fechaDesde)) {
            throw new OperacionInvalidaException(
                    "fechaHasta no puede ser menor que fechaDesde");
        }
    }

    private BigDecimal valorSeguro(BigDecimal valor) {
        return valor != null ? valor : BigDecimal.ZERO;
    }

    private String csvValue(Object valor) {
        if (valor == null) {
            return "";
        }
        String texto = String.valueOf(valor);
        String escapado = texto.replace("\"", "\"\"");
        return "\"" + escapado + "\"";
    }
}

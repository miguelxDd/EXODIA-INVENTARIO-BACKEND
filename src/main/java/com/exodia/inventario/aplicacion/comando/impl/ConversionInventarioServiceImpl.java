package com.exodia.inventario.aplicacion.comando.impl;

import com.exodia.inventario.aplicacion.comando.BarcodeService;
import com.exodia.inventario.aplicacion.comando.ConversionInventarioService;
import com.exodia.inventario.aplicacion.comando.OperacionService;
import com.exodia.inventario.aplicacion.consulta.StockQueryService;
import com.exodia.inventario.domain.enums.TipoOperacionCodigo;
import com.exodia.inventario.domain.enums.TipoReferencia;
import com.exodia.inventario.domain.evento.ConversionInventarioRealizadaEvent;
import com.exodia.inventario.domain.modelo.catalogo.ConversionUnidad;
import com.exodia.inventario.domain.modelo.catalogo.Unidad;
import com.exodia.inventario.domain.modelo.contenedor.Contenedor;
import com.exodia.inventario.domain.modelo.contenedor.Operacion;
import com.exodia.inventario.excepcion.ConversionNoEncontradaException;
import com.exodia.inventario.excepcion.EntidadNoEncontradaException;
import com.exodia.inventario.excepcion.OperacionInvalidaException;
import com.exodia.inventario.interfaz.dto.peticion.ConvertirInventarioRequest;
import com.exodia.inventario.interfaz.dto.respuesta.ConversionInventarioResponse;
import com.exodia.inventario.repositorio.catalogo.ConversionUnidadRepository;
import com.exodia.inventario.repositorio.catalogo.UnidadRepository;
import com.exodia.inventario.repositorio.contenedor.ContenedorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversionInventarioServiceImpl implements ConversionInventarioService {

    private final ContenedorRepository contenedorRepository;
    private final UnidadRepository unidadRepository;
    private final ConversionUnidadRepository conversionUnidadRepository;
    private final StockQueryService stockQueryService;
    private final OperacionService operacionService;
    private final BarcodeService barcodeService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public ConversionInventarioResponse convertir(Long empresaId, ConvertirInventarioRequest request) {
        Contenedor contenedorOrigen = contenedorRepository.findByIdForUpdate(request.contenedorId())
                .filter(c -> c.getEmpresa().getId().equals(empresaId))
                .filter(c -> Boolean.TRUE.equals(c.getActivo()))
                .orElseThrow(() -> new EntidadNoEncontradaException("Contenedor", request.contenedorId()));

        Unidad unidadDestino = unidadRepository.findById(request.unidadDestinoId())
                .filter(u -> u.getEmpresa().getId().equals(empresaId))
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .orElseThrow(() -> new EntidadNoEncontradaException("Unidad", request.unidadDestinoId()));

        Long unidadOrigenId = contenedorOrigen.getUnidad().getId();
        if (unidadOrigenId.equals(unidadDestino.getId())) {
            throw new OperacionInvalidaException("La unidad destino debe ser diferente a la unidad origen");
        }

        BigDecimal stockActual = stockQueryService.obtenerStockContenedor(contenedorOrigen.getId());
        if (stockActual.compareTo(BigDecimal.ZERO) <= 0) {
            throw new OperacionInvalidaException("El contenedor no tiene stock para convertir");
        }

        BigDecimal cantidadReservada = stockQueryService.obtenerCantidadReservada(contenedorOrigen.getId());
        if (cantidadReservada.compareTo(BigDecimal.ZERO) > 0) {
            throw new OperacionInvalidaException(
                    "No se puede convertir un contenedor con reservas activas. Libere las reservas primero.");
        }

        if (request.cantidadOrigen().compareTo(stockActual) > 0) {
            throw new OperacionInvalidaException(String.format(
                    "La cantidad a convertir (%s) excede el stock actual (%s)",
                    request.cantidadOrigen(), stockActual));
        }

        ConversionUnidad conversion = conversionUnidadRepository
                .findByEmpresaIdAndUnidadOrigenIdAndUnidadDestinoIdAndProductoId(
                        empresaId, unidadOrigenId, unidadDestino.getId(), contenedorOrigen.getProductoId())
                .or(() -> conversionUnidadRepository.findByEmpresaIdAndUnidadOrigenIdAndUnidadDestinoIdAndProductoIdIsNull(
                        empresaId, unidadOrigenId, unidadDestino.getId()))
                .filter(ConversionUnidad::getActivo)
                .orElseThrow(() -> new ConversionNoEncontradaException(unidadOrigenId, unidadDestino.getId()));

        BigDecimal cantidadDestino = conversion.convertir(request.cantidadOrigen())
                .setScale(6, RoundingMode.HALF_UP);
        if (cantidadDestino.compareTo(BigDecimal.ZERO) <= 0) {
            throw new OperacionInvalidaException("La cantidad resultante de la conversion debe ser mayor a cero");
        }

        BigDecimal precioOrigen = contenedorOrigen.getPrecioUnitario();
        BigDecimal valorTotalOrigen = precioOrigen.multiply(request.cantidadOrigen());
        BigDecimal precioDestino = valorTotalOrigen.divide(cantidadDestino, 6, RoundingMode.HALF_UP);
        boolean conversionTotal = request.cantidadOrigen().compareTo(stockActual) == 0;

        String comentario = request.comentarios() != null && !request.comentarios().isBlank()
                ? request.comentarios()
                : String.format("Conversion %s -> %s",
                contenedorOrigen.getUnidad().getCodigo(), unidadDestino.getCodigo());

        Operacion operacionSalida = operacionService.crearOperacion(
                contenedorOrigen,
                TipoOperacionCodigo.SALIDA_CONVERSION,
                request.cantidadOrigen(),
                comentario,
                TipoReferencia.CONVERSION,
                contenedorOrigen.getId(),
                null);

        Contenedor contenedorDestino;
        if (conversionTotal) {
            contenedorOrigen.setUnidad(unidadDestino);
            contenedorOrigen.setPrecioUnitario(precioDestino);
            contenedorRepository.save(contenedorOrigen);
            contenedorDestino = contenedorOrigen;
        } else {
            String barcodeDestino = barcodeService.generarBarcode(empresaId);
            contenedorDestino = Contenedor.builder()
                    .empresa(contenedorOrigen.getEmpresa())
                    .codigoBarras(barcodeDestino)
                    .productoId(contenedorOrigen.getProductoId())
                    .proveedorId(contenedorOrigen.getProveedorId())
                    .productoProveedorId(contenedorOrigen.getProductoProveedorId())
                    .unidad(unidadDestino)
                    .bodega(contenedorOrigen.getBodega())
                    .ubicacion(contenedorOrigen.getUbicacion())
                    .precioUnitario(precioDestino)
                    .lote(contenedorOrigen.getLote())
                    .numeroLote(contenedorOrigen.getNumeroLote())
                    .fechaVencimiento(contenedorOrigen.getFechaVencimiento())
                    .numeroSerie(contenedorOrigen.getNumeroSerie())
                    .marcaId(contenedorOrigen.getMarcaId())
                    .origenId(contenedorOrigen.getOrigenId())
                    .infoGarantia(contenedorOrigen.getInfoGarantia())
                    .estado(contenedorOrigen.getEstado())
                    .build();
            contenedorDestino = contenedorRepository.save(contenedorDestino);
        }

        Operacion operacionEntrada = operacionService.crearOperacion(
                contenedorDestino,
                TipoOperacionCodigo.ENTRADA_CONVERSION,
                cantidadDestino,
                comentario,
                TipoReferencia.CONVERSION,
                contenedorOrigen.getId(),
                null);

        eventPublisher.publishEvent(new ConversionInventarioRealizadaEvent(
                empresaId,
                contenedorOrigen.getId(),
                contenedorDestino.getId(),
                contenedorOrigen.getProductoId(),
                unidadOrigenId,
                unidadDestino.getId(),
                request.cantidadOrigen(),
                cantidadDestino,
                conversionTotal));

        log.info("Conversion inventario completada: contenedorOrigen={}, contenedorDestino={}, total={}",
                contenedorOrigen.getId(), contenedorDestino.getId(), conversionTotal);

        return new ConversionInventarioResponse(
                contenedorOrigen.getId(),
                contenedorOrigen.getCodigoBarras(),
                contenedorDestino.getId(),
                contenedorDestino.getCodigoBarras(),
                unidadOrigenId,
                unidadDestino.getId(),
                request.cantidadOrigen(),
                cantidadDestino,
                precioOrigen,
                precioDestino,
                conversionTotal,
                operacionSalida.getId(),
                operacionEntrada.getId());
    }
}

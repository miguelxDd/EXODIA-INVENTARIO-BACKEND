package com.exodia.inventario.aplicacion.comando.impl;

import com.exodia.inventario.aplicacion.comando.OperacionService;
import com.exodia.inventario.domain.enums.TipoOperacionCodigo;
import com.exodia.inventario.domain.enums.TipoReferencia;
import com.exodia.inventario.domain.modelo.catalogo.TipoOperacion;
import com.exodia.inventario.domain.modelo.contenedor.Contenedor;
import com.exodia.inventario.domain.modelo.contenedor.Operacion;
import com.exodia.inventario.domain.servicio.CalculadorStock;
import com.exodia.inventario.excepcion.OperacionInvalidaException;
import com.exodia.inventario.repositorio.catalogo.TipoOperacionRepository;
import com.exodia.inventario.repositorio.contenedor.OperacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Implementacion del servicio central de operaciones del kardex.
 * Punto unico obligatorio para toda creacion de operaciones.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OperacionServiceImpl implements OperacionService {

    private final OperacionRepository operacionRepository;
    private final TipoOperacionRepository tipoOperacionRepository;
    private final CalculadorStock calculadorStock;

    @Override
    @Transactional
    public Operacion crearOperacion(Contenedor contenedor,
                                     TipoOperacionCodigo tipoCodigo,
                                     BigDecimal cantidad,
                                     String comentarios,
                                     TipoReferencia tipoReferencia,
                                     Long referenciaId,
                                     Long referenciaLineaId) {

        validarParametros(contenedor, tipoCodigo, cantidad);

        TipoOperacion tipoOperacion = tipoOperacionRepository.findByCodigo(tipoCodigo.getCodigo())
                .orElseThrow(() -> new OperacionInvalidaException(
                        "Tipo de operacion no encontrado en catalogo: " + tipoCodigo.getCodigo()));

        BigDecimal cantidadConSigno = calculadorStock.aplicarSigno(cantidad, tipoCodigo.getSigno());

        Operacion operacion = Operacion.builder()
                .empresa(contenedor.getEmpresa())
                .contenedor(contenedor)
                .codigoBarras(contenedor.getCodigoBarras())
                .productoId(contenedor.getProductoId())
                .bodega(contenedor.getBodega())
                .ubicacion(contenedor.getUbicacion())
                .unidad(contenedor.getUnidad())
                .tipoOperacion(tipoOperacion)
                .cantidad(cantidadConSigno)
                .precioUnitario(contenedor.getPrecioUnitario())
                .numeroLote(contenedor.getNumeroLote())
                .fechaVencimiento(contenedor.getFechaVencimiento())
                .proveedorId(contenedor.getProveedorId())
                .tipoReferencia(tipoReferencia)
                .referenciaId(referenciaId)
                .referenciaLineaId(referenciaLineaId)
                .comentarios(comentarios)
                .build();

        operacion = operacionRepository.save(operacion);

        log.info("Operacion {} creada: tipo={}, contenedor={}, cantidad={}, barcode={}",
                operacion.getId(), tipoCodigo.getCodigo(),
                contenedor.getId(), cantidadConSigno.toPlainString(),
                contenedor.getCodigoBarras());

        return operacion;
    }

    @Override
    @Transactional
    public Operacion crearOperacion(Contenedor contenedor,
                                     TipoOperacionCodigo tipoCodigo,
                                     BigDecimal cantidad,
                                     String comentarios) {
        return crearOperacion(contenedor, tipoCodigo, cantidad, comentarios, null, null, null);
    }

    private void validarParametros(Contenedor contenedor,
                                    TipoOperacionCodigo tipoCodigo,
                                    BigDecimal cantidad) {
        if (contenedor == null) {
            throw new OperacionInvalidaException("El contenedor no puede ser nulo");
        }
        if (tipoCodigo == null) {
            throw new OperacionInvalidaException("El tipo de operacion no puede ser nulo");
        }
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new OperacionInvalidaException(
                    "La cantidad debe ser mayor a cero: " + cantidad);
        }
    }
}

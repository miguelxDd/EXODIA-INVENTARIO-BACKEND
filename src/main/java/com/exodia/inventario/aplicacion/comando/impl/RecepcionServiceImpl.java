package com.exodia.inventario.aplicacion.comando.impl;

import com.exodia.inventario.aplicacion.comando.BarcodeService;
import com.exodia.inventario.aplicacion.comando.LoteService;
import com.exodia.inventario.aplicacion.comando.OperacionService;
import com.exodia.inventario.aplicacion.comando.RecepcionService;
import com.exodia.inventario.infraestructura.integracion.ProductoAdapter;
import com.exodia.inventario.domain.modelo.extension.ConfiguracionProducto;
import com.exodia.inventario.repositorio.extension.ConfiguracionProductoRepository;
import com.exodia.inventario.domain.enums.EstadoContenedorCodigo;
import com.exodia.inventario.domain.enums.TipoOperacionCodigo;
import com.exodia.inventario.domain.enums.TipoRecepcion;
import com.exodia.inventario.domain.enums.TipoReferencia;
import com.exodia.inventario.domain.evento.InventarioRecibidoEvent;
import com.exodia.inventario.domain.modelo.catalogo.Bodega;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;
import com.exodia.inventario.domain.modelo.catalogo.EstadoContenedor;
import com.exodia.inventario.domain.modelo.catalogo.Ubicacion;
import com.exodia.inventario.domain.modelo.catalogo.Unidad;
import com.exodia.inventario.domain.modelo.contenedor.Contenedor;
import com.exodia.inventario.domain.modelo.contenedor.Lote;
import com.exodia.inventario.domain.modelo.contenedor.Operacion;
import com.exodia.inventario.domain.modelo.recepcion.Recepcion;
import com.exodia.inventario.domain.modelo.recepcion.RecepcionLinea;
import com.exodia.inventario.excepcion.EntidadNoEncontradaException;
import com.exodia.inventario.excepcion.OperacionInvalidaException;
import com.exodia.inventario.interfaz.dto.peticion.CrearRecepcionRequest;
import com.exodia.inventario.interfaz.dto.peticion.RecepcionLineaRequest;
import com.exodia.inventario.interfaz.dto.respuesta.RecepcionResponse;
import com.exodia.inventario.interfaz.mapeador.RecepcionMapeador;
import com.exodia.inventario.repositorio.catalogo.BodegaRepository;
import com.exodia.inventario.repositorio.catalogo.EmpresaRepository;
import com.exodia.inventario.repositorio.catalogo.EstadoContenedorRepository;
import com.exodia.inventario.repositorio.catalogo.UbicacionRepository;
import com.exodia.inventario.repositorio.catalogo.UnidadRepository;
import com.exodia.inventario.repositorio.contenedor.ContenedorRepository;
import com.exodia.inventario.repositorio.recepcion.RecepcionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecepcionServiceImpl implements RecepcionService {

    private final RecepcionRepository recepcionRepository;
    private final EmpresaRepository empresaRepository;
    private final BodegaRepository bodegaRepository;
    private final UbicacionRepository ubicacionRepository;
    private final UnidadRepository unidadRepository;
    private final ContenedorRepository contenedorRepository;
    private final EstadoContenedorRepository estadoContenedorRepository;
    private final OperacionService operacionService;
    private final BarcodeService barcodeService;
    private final LoteService loteService;
    private final ConfiguracionProductoRepository configuracionProductoRepository;
    private final ProductoAdapter productoAdapter;
    private final RecepcionMapeador recepcionMapeador;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public RecepcionResponse crear(Long empresaId, CrearRecepcionRequest request) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EntidadNoEncontradaException("Empresa", empresaId));

        Bodega bodega = bodegaRepository.findById(request.bodegaId())
                .filter(b -> b.getEmpresa().getId().equals(empresaId))
                .filter(b -> Boolean.TRUE.equals(b.getActivo()))
                .orElseThrow(() -> new EntidadNoEncontradaException("Bodega", request.bodegaId()));

        EstadoContenedor estadoDisponible = estadoContenedorRepository
                .findByCodigo(EstadoContenedorCodigo.DISPONIBLE.getCodigo())
                .orElseThrow(() -> new OperacionInvalidaException(
                        "Estado DISPONIBLE no encontrado en catalogo"));

        // Generar numero de recepcion
        String numeroRecepcion = generarNumeroRecepcion(empresaId);

        Recepcion recepcion = Recepcion.builder()
                .empresa(empresa)
                .numeroRecepcion(numeroRecepcion)
                .bodega(bodega)
                .tipoRecepcion(TipoRecepcion.valueOf(request.tipoRecepcion()))
                .referenciaOrigenId(request.referenciaOrigenId())
                .proveedorId(request.proveedorId())
                .comentarios(request.comentarios())
                .build();

        recepcion = recepcionRepository.save(recepcion);

        List<Long> contenedorIds = new ArrayList<>();
        AtomicInteger lineaNum = new AtomicInteger(0);

        for (RecepcionLineaRequest lineaReq : request.lineas()) {
            lineaNum.incrementAndGet();
            RecepcionLinea linea = procesarLinea(
                    recepcion, empresa, bodega, estadoDisponible, lineaReq, request.proveedorId());
            recepcion.getLineas().add(linea);
            contenedorIds.add(linea.getContenedor().getId());
        }

        recepcion = recepcionRepository.save(recepcion);

        log.info("Recepcion {} creada con {} lineas en bodega {} para empresa {}",
                numeroRecepcion, request.lineas().size(), bodega.getCodigo(), empresaId);

        // Publicar evento
        eventPublisher.publishEvent(new InventarioRecibidoEvent(
                recepcion.getId(), empresaId, bodega.getId(),
                contenedorIds, request.lineas().size()));

        return recepcionMapeador.toResponse(recepcion);
    }

    @Override
    @Transactional(readOnly = true)
    public RecepcionResponse obtenerPorId(Long empresaId, Long recepcionId) {
        Recepcion recepcion = recepcionRepository.findById(recepcionId)
                .filter(r -> r.getEmpresa().getId().equals(empresaId))
                .filter(r -> Boolean.TRUE.equals(r.getActivo()))
                .orElseThrow(() -> new EntidadNoEncontradaException("Recepcion", recepcionId));
        return recepcionMapeador.toResponse(recepcion);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RecepcionResponse> listarPorEmpresa(Long empresaId, Pageable pageable) {
        return recepcionRepository.findByEmpresaIdOrderByCreadoEnDesc(empresaId, pageable)
                .map(recepcionMapeador::toResponse);
    }

    private RecepcionLinea procesarLinea(Recepcion recepcion, Empresa empresa, Bodega bodega,
                                         EstadoContenedor estadoDisponible,
                                         RecepcionLineaRequest lineaReq, Long proveedorIdHeader) {

        // Validar que el producto existe en el servicio de catalogos
        if (!productoAdapter.existeProducto(empresa.getId(), lineaReq.productoId())) {
            throw new EntidadNoEncontradaException("Producto", lineaReq.productoId());
        }

        Unidad unidad = unidadRepository.findById(lineaReq.unidadId())
                .filter(u -> u.getEmpresa().getId().equals(empresa.getId()))
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .orElseThrow(() -> new EntidadNoEncontradaException("Unidad", lineaReq.unidadId()));

        Ubicacion ubicacion = ubicacionRepository.findById(lineaReq.ubicacionId())
                .filter(u -> u.getBodega().getId().equals(bodega.getId()))
                .orElseThrow(() -> new EntidadNoEncontradaException("Ubicacion", lineaReq.ubicacionId()));

        Long proveedorId = lineaReq.proveedorId() != null ? lineaReq.proveedorId() : proveedorIdHeader;
        BigDecimal precioUnitario = lineaReq.precioUnitario() != null
                ? lineaReq.precioUnitario() : BigDecimal.ZERO;

        // Validar requisitos de lote/vencimiento/unidadBase segun ConfiguracionProducto
        configuracionProductoRepository
                .findByEmpresaIdAndProductoId(empresa.getId(), lineaReq.productoId())
                .ifPresent(configProd -> {
                    if (Boolean.TRUE.equals(configProd.getManejaLote())
                            && (lineaReq.numeroLote() == null || lineaReq.numeroLote().isBlank())) {
                        throw new OperacionInvalidaException(String.format(
                                "Producto %d requiere numero de lote segun configuracion", lineaReq.productoId()));
                    }
                    if (Boolean.TRUE.equals(configProd.getManejaVencimiento())
                            && lineaReq.fechaVencimiento() == null) {
                        throw new OperacionInvalidaException(String.format(
                                "Producto %d requiere fecha de vencimiento segun configuracion", lineaReq.productoId()));
                    }
                    if (configProd.getUnidadBase() != null
                            && !configProd.getUnidadBase().getId().equals(lineaReq.unidadId())) {
                        throw new OperacionInvalidaException(String.format(
                                "Producto %d debe recibirse en unidad base %s (id=%d), se envio unidad %d",
                                lineaReq.productoId(), configProd.getUnidadBase().getAbreviatura(),
                                configProd.getUnidadBase().getId(), lineaReq.unidadId()));
                    }
                });

        // Manejar lote si aplica
        Lote lote = null;
        if (lineaReq.numeroLote() != null && !lineaReq.numeroLote().isBlank()) {
            lote = loteService.buscarOCrear(empresa.getId(), lineaReq.productoId(),
                    lineaReq.numeroLote(), lineaReq.fechaVencimiento(), proveedorId);
        }

        // Crear o reutilizar contenedor
        Contenedor contenedor;
        boolean barcodeGenerado;
        boolean barcodeReutilizado;

        if (lineaReq.codigoBarras() != null && !lineaReq.codigoBarras().isBlank()) {
            // Reutilizar contenedor existente
            contenedor = contenedorRepository
                    .findByEmpresaIdAndCodigoBarras(empresa.getId(), lineaReq.codigoBarras())
                    .orElseThrow(() -> new OperacionInvalidaException(
                            "Contenedor con barcode " + lineaReq.codigoBarras() + " no encontrado"));

            // Validar que el contenedor coincide con producto, unidad y ubicacion
            if (!contenedor.getProductoId().equals(lineaReq.productoId())) {
                throw new OperacionInvalidaException(String.format(
                        "Contenedor %s pertenece a producto %d, no coincide con producto %d",
                        lineaReq.codigoBarras(), contenedor.getProductoId(), lineaReq.productoId()));
            }
            if (!contenedor.getUnidad().getId().equals(lineaReq.unidadId())) {
                throw new OperacionInvalidaException(String.format(
                        "Contenedor %s tiene unidad %d, no coincide con unidad %d",
                        lineaReq.codigoBarras(), contenedor.getUnidad().getId(), lineaReq.unidadId()));
            }
            if (!contenedor.getUbicacion().getId().equals(lineaReq.ubicacionId())) {
                throw new OperacionInvalidaException(String.format(
                        "Contenedor %s esta en ubicacion %d, no coincide con ubicacion %d",
                        lineaReq.codigoBarras(), contenedor.getUbicacion().getId(), lineaReq.ubicacionId()));
            }
            // Validar metadatos adicionales del contenedor (usar proveedorId resuelto, no solo linea)
            if (proveedorId != null && contenedor.getProveedorId() != null
                    && !contenedor.getProveedorId().equals(proveedorId)) {
                throw new OperacionInvalidaException(String.format(
                        "Contenedor %s tiene proveedor %d, no coincide con proveedor %d",
                        lineaReq.codigoBarras(), contenedor.getProveedorId(), proveedorId));
            }
            if (precioUnitario.compareTo(BigDecimal.ZERO) > 0
                    && contenedor.getPrecioUnitario().compareTo(BigDecimal.ZERO) > 0
                    && contenedor.getPrecioUnitario().compareTo(precioUnitario) != 0) {
                throw new OperacionInvalidaException(String.format(
                        "Contenedor %s tiene precio %s, no coincide con precio %s",
                        lineaReq.codigoBarras(), contenedor.getPrecioUnitario(), precioUnitario));
            }
            if (lineaReq.numeroLote() != null && !lineaReq.numeroLote().isBlank()
                    && contenedor.getNumeroLote() != null
                    && !contenedor.getNumeroLote().equals(lineaReq.numeroLote())) {
                throw new OperacionInvalidaException(String.format(
                        "Contenedor %s tiene lote '%s', no coincide con lote '%s'",
                        lineaReq.codigoBarras(), contenedor.getNumeroLote(), lineaReq.numeroLote()));
            }
            if (lineaReq.fechaVencimiento() != null
                    && contenedor.getFechaVencimiento() != null
                    && !contenedor.getFechaVencimiento().equals(lineaReq.fechaVencimiento())) {
                throw new OperacionInvalidaException(String.format(
                        "Contenedor %s tiene vencimiento %s, no coincide con %s",
                        lineaReq.codigoBarras(), contenedor.getFechaVencimiento(), lineaReq.fechaVencimiento()));
            }

            barcodeGenerado = false;
            barcodeReutilizado = true;
        } else {
            // Generar nuevo contenedor con barcode
            String barcode = barcodeService.generarBarcode(empresa.getId());
            contenedor = Contenedor.builder()
                    .empresa(empresa)
                    .codigoBarras(barcode)
                    .productoId(lineaReq.productoId())
                    .proveedorId(proveedorId)
                    .unidad(unidad)
                    .bodega(bodega)
                    .ubicacion(ubicacion)
                    .precioUnitario(precioUnitario)
                    .lote(lote)
                    .numeroLote(lineaReq.numeroLote())
                    .fechaVencimiento(lineaReq.fechaVencimiento())
                    .estado(estadoDisponible)
                    .build();
            contenedor = contenedorRepository.save(contenedor);
            barcodeGenerado = true;
            barcodeReutilizado = false;
        }

        // Crear operacion RECEPCION en el kardex
        Operacion operacion = operacionService.crearOperacion(
                contenedor,
                TipoOperacionCodigo.RECEPCION,
                lineaReq.cantidad(),
                "Recepcion " + recepcion.getNumeroRecepcion(),
                TipoReferencia.RECEPCION,
                recepcion.getId(),
                null);

        // Crear linea de recepcion
        return RecepcionLinea.builder()
                .recepcion(recepcion)
                .contenedor(contenedor)
                .productoId(lineaReq.productoId())
                .unidad(unidad)
                .ubicacion(ubicacion)
                .cantidad(lineaReq.cantidad())
                .precioUnitario(precioUnitario)
                .numeroLote(lineaReq.numeroLote())
                .fechaVencimiento(lineaReq.fechaVencimiento())
                .barcodeGenerado(barcodeGenerado)
                .barcodeReutilizado(barcodeReutilizado)
                .operacion(operacion)
                .build();
    }

    private String generarNumeroRecepcion(Long empresaId) {
        return barcodeService.generarBarcode(empresaId, "REC");
    }
}

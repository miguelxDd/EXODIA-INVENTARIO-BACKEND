package com.exodia.inventario.aplicacion.comando.impl;

import com.exodia.inventario.aplicacion.comando.BarcodeService;
import com.exodia.inventario.aplicacion.comando.OperacionService;
import com.exodia.inventario.aplicacion.comando.PickingService;
import com.exodia.inventario.aplicacion.consulta.StockQueryService;
import com.exodia.inventario.domain.enums.EstadoContenedorCodigo;
import com.exodia.inventario.domain.enums.TipoOperacionCodigo;
import com.exodia.inventario.domain.enums.TipoReferencia;
import com.exodia.inventario.domain.evento.PickingCompletadoEvent;
import com.exodia.inventario.domain.modelo.catalogo.Bodega;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;
import com.exodia.inventario.domain.modelo.catalogo.EstadoContenedor;
import com.exodia.inventario.domain.modelo.catalogo.Unidad;
import com.exodia.inventario.domain.modelo.contenedor.Contenedor;
import com.exodia.inventario.domain.modelo.contenedor.Operacion;
import com.exodia.inventario.domain.modelo.picking.OrdenPicking;
import com.exodia.inventario.domain.modelo.picking.PickingLinea;
import com.exodia.inventario.domain.politica.PoliticaDeduccionStock;
import com.exodia.inventario.domain.servicio.PoliticaFEFO;
import com.exodia.inventario.excepcion.EntidadNoEncontradaException;
import com.exodia.inventario.excepcion.OperacionInvalidaException;
import com.exodia.inventario.excepcion.StockInsuficienteException;
import com.exodia.inventario.interfaz.dto.peticion.CrearOrdenPickingRequest;
import com.exodia.inventario.interfaz.dto.peticion.PickingLineaRequest;
import com.exodia.inventario.interfaz.dto.respuesta.OrdenPickingResponse;
import com.exodia.inventario.interfaz.mapeador.PickingMapeador;
import com.exodia.inventario.repositorio.catalogo.BodegaRepository;
import com.exodia.inventario.repositorio.catalogo.EmpresaRepository;
import com.exodia.inventario.repositorio.catalogo.UnidadRepository;
import com.exodia.inventario.repositorio.catalogo.EstadoContenedorRepository;
import com.exodia.inventario.repositorio.contenedor.ContenedorRepository;
import com.exodia.inventario.repositorio.picking.OrdenPickingRepository;
import com.exodia.inventario.repositorio.proyeccion.ContenedorStockProjection;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class PickingServiceImpl implements PickingService {

    private final OrdenPickingRepository ordenPickingRepository;
    private final EmpresaRepository empresaRepository;
    private final BodegaRepository bodegaRepository;
    private final UnidadRepository unidadRepository;
    private final ContenedorRepository contenedorRepository;
    private final EstadoContenedorRepository estadoContenedorRepository;
    private final OperacionService operacionService;
    private final StockQueryService stockQueryService;
    private final BarcodeService barcodeService;
    private final PoliticaFEFO politicaFEFO;
    private final PoliticaDeduccionStock politicaDeduccionStock;
    private final PickingMapeador pickingMapeador;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public OrdenPickingResponse crear(Long empresaId, CrearOrdenPickingRequest request) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EntidadNoEncontradaException("Empresa", empresaId));

        Bodega bodega = bodegaRepository.findById(request.bodegaId())
                .filter(b -> b.getEmpresa().getId().equals(empresaId))
                .filter(b -> Boolean.TRUE.equals(b.getActivo()))
                .orElseThrow(() -> new EntidadNoEncontradaException("Bodega", request.bodegaId()));

        String numeroOrden = barcodeService.generarBarcode(empresaId, "PKG");

        OrdenPicking orden = OrdenPicking.builder()
                .empresa(empresa)
                .numeroOrden(numeroOrden)
                .bodega(bodega)
                .tipoPicking(request.tipoPicking())
                .tipoReferencia(request.tipoReferencia())
                .referenciaId(request.referenciaId())
                .comentarios(request.comentarios())
                .build();

        for (PickingLineaRequest lineaReq : request.lineas()) {
            Unidad unidad = unidadRepository.findById(lineaReq.unidadId())
                    .filter(u -> u.getEmpresa().getId().equals(empresaId))
                    .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                    .orElseThrow(() -> new EntidadNoEncontradaException("Unidad", lineaReq.unidadId()));

            PickingLinea linea = PickingLinea.builder()
                    .ordenPicking(orden)
                    .productoId(lineaReq.productoId())
                    .unidad(unidad)
                    .cantidadSolicitada(lineaReq.cantidadSolicitada())
                    .build();

            orden.getLineas().add(linea);
        }

        orden = ordenPickingRepository.save(orden);

        log.info("Orden de picking {} creada con {} lineas en bodega {} para empresa {}",
                numeroOrden, request.lineas().size(), bodega.getCodigo(), empresaId);

        return pickingMapeador.toResponse(orden);
    }

    @Override
    @Transactional
    public OrdenPickingResponse ejecutar(Long empresaId, Long ordenId) {
        OrdenPicking orden = ordenPickingRepository.findById(ordenId)
                .filter(o -> o.getEmpresa().getId().equals(empresaId))
                .orElseThrow(() -> new EntidadNoEncontradaException("OrdenPicking", ordenId));

        if (!"PENDIENTE".equals(orden.getEstado())) {
            throw new OperacionInvalidaException(
                    "Solo se puede ejecutar una orden en estado PENDIENTE, actual: " + orden.getEstado());
        }

        orden.setEstado("EN_PROCESO");

        int lineasProcesadas = 0;

        for (PickingLinea linea : orden.getLineas()) {
            procesarLineaPicking(orden, linea);
            lineasProcesadas++;
        }

        orden.setEstado("COMPLETADO");
        orden = ordenPickingRepository.save(orden);

        log.info("Orden de picking {} ejecutada: {} lineas procesadas", orden.getNumeroOrden(), lineasProcesadas);

        eventPublisher.publishEvent(new PickingCompletadoEvent(
                orden.getId(), empresaId, orden.getBodega().getId(), lineasProcesadas));

        return pickingMapeador.toResponse(orden);
    }

    @Override
    @Transactional(readOnly = true)
    public OrdenPickingResponse obtenerPorId(Long empresaId, Long ordenId) {
        OrdenPicking orden = ordenPickingRepository.findById(ordenId)
                .filter(o -> o.getEmpresa().getId().equals(empresaId))
                .orElseThrow(() -> new EntidadNoEncontradaException("OrdenPicking", ordenId));
        return pickingMapeador.toResponse(orden);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrdenPickingResponse> listarPorEmpresa(Long empresaId, Pageable pageable) {
        return ordenPickingRepository.findByEmpresaIdOrderByCreadoEnDesc(empresaId, pageable)
                .map(pickingMapeador::toResponse);
    }

    @Override
    @Transactional
    public OrdenPickingResponse cancelar(Long empresaId, Long ordenId) {
        OrdenPicking orden = ordenPickingRepository.findById(ordenId)
                .filter(o -> o.getEmpresa().getId().equals(empresaId))
                .orElseThrow(() -> new EntidadNoEncontradaException("OrdenPicking", ordenId));

        if (!"PENDIENTE".equals(orden.getEstado())) {
            throw new OperacionInvalidaException(
                    "Solo se puede cancelar una orden en estado PENDIENTE, actual: " + orden.getEstado());
        }

        orden.setEstado("CANCELADO");
        orden = ordenPickingRepository.save(orden);

        log.info("Orden de picking {} cancelada", orden.getNumeroOrden());

        return pickingMapeador.toResponse(orden);
    }

    private void procesarLineaPicking(OrdenPicking orden, PickingLinea linea) {
        Long bodegaId = orden.getBodega().getId();
        Long empresaId = orden.getEmpresa().getId();

        // Obtener contenedores disponibles FEFO
        List<ContenedorStockProjection> disponibles = stockQueryService
                .obtenerContenedoresDisponiblesFEFO(empresaId, linea.getProductoId(), bodegaId);

        List<PoliticaFEFO.ContenedorConStock> contenedoresConStock = disponibles.stream()
                .map(p -> new PoliticaFEFO.ContenedorConStock(
                        p.getContenedorId(), p.getFechaVencimiento(), null, p.getCantidadDisponible()))
                .toList();

        List<PoliticaFEFO.AsignacionContenedor> asignaciones = politicaFEFO
                .seleccionarContenedores(contenedoresConStock, linea.getCantidadSolicitada());

        BigDecimal totalAsignado = asignaciones.stream()
                .map(PoliticaFEFO.AsignacionContenedor::cantidad)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalAsignado.compareTo(linea.getCantidadSolicitada()) < 0) {
            throw new StockInsuficienteException(
                    linea.getProductoId(), linea.getCantidadSolicitada(), totalAsignado);
        }

        // Ordenar IDs para locks secuenciales
        List<Long> idsOrdenados = asignaciones.stream()
                .map(PoliticaFEFO.AsignacionContenedor::contenedorId)
                .sorted()
                .toList();

        // Adquirir locks pesimistas en orden
        List<Contenedor> contenedoresLocked = new ArrayList<>();
        for (Long id : idsOrdenados) {
            Contenedor c = contenedorRepository.findByIdForUpdate(id)
                    .orElseThrow(() -> new EntidadNoEncontradaException("Contenedor", id));
            contenedoresLocked.add(c);
        }

        // Procesar asignaciones — tomar del primer contenedor FEFO
        for (PoliticaFEFO.AsignacionContenedor asignacion : asignaciones) {
            Contenedor contenedor = contenedoresLocked.stream()
                    .filter(c -> c.getId().equals(asignacion.contenedorId()))
                    .findFirst()
                    .orElseThrow();

            // Validar stock disponible con lock
            BigDecimal stockDisponible = stockQueryService.obtenerStockDisponible(contenedor.getId());

            PoliticaDeduccionStock.ResultadoValidacion resultado = politicaDeduccionStock.evaluar(
                    contenedor.getEstado().getCodigo(), stockDisponible, asignacion.cantidad());

            if (!resultado.valido()) {
                throw new StockInsuficienteException(
                        contenedor.getId(), asignacion.cantidad(), stockDisponible);
            }

            Operacion operacion = operacionService.crearOperacion(
                    contenedor,
                    TipoOperacionCodigo.PICKING,
                    asignacion.cantidad(),
                    "Picking " + orden.getNumeroOrden(),
                    TipoReferencia.PICKING,
                    orden.getId(),
                    linea.getId());

            linea.setContenedor(contenedor);
            linea.setOperacion(operacion);
            linea.setCantidadPickeada(asignacion.cantidad());

            // Verificar si el contenedor queda agotado
            BigDecimal stockRestante = stockDisponible.subtract(asignacion.cantidad());
            if (stockRestante.compareTo(BigDecimal.ZERO) <= 0) {
                EstadoContenedor estadoAgotado = estadoContenedorRepository
                        .findByCodigo(EstadoContenedorCodigo.AGOTADO.getCodigo())
                        .orElseThrow(() -> new OperacionInvalidaException(
                                "Estado AGOTADO no encontrado en catalogo"));
                contenedor.setEstado(estadoAgotado);
                contenedorRepository.save(contenedor);
            }
        }
    }
}

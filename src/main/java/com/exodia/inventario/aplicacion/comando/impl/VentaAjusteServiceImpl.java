package com.exodia.inventario.aplicacion.comando.impl;

import com.exodia.inventario.aplicacion.comando.BarcodeService;
import com.exodia.inventario.aplicacion.comando.ConfiguracionEmpresaService;
import com.exodia.inventario.aplicacion.comando.OperacionService;
import com.exodia.inventario.aplicacion.comando.VentaAjusteService;
import com.exodia.inventario.aplicacion.consulta.StockQueryService;
import com.exodia.inventario.domain.enums.EstadoContenedorCodigo;
import com.exodia.inventario.domain.enums.TipoOperacionCodigo;
import com.exodia.inventario.domain.enums.TipoReferencia;
import com.exodia.inventario.domain.evento.VentaFacturadaAjustadaEvent;
import com.exodia.inventario.domain.modelo.ajuste.Ajuste;
import com.exodia.inventario.domain.modelo.ajuste.AjusteLinea;
import com.exodia.inventario.domain.modelo.catalogo.Bodega;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;
import com.exodia.inventario.domain.modelo.catalogo.EstadoContenedor;
import com.exodia.inventario.domain.modelo.catalogo.TipoAjuste;
import com.exodia.inventario.domain.modelo.catalogo.Unidad;
import com.exodia.inventario.domain.modelo.contenedor.Contenedor;
import com.exodia.inventario.domain.modelo.contenedor.Operacion;
import com.exodia.inventario.domain.modelo.extension.ConfiguracionEmpresa;
import com.exodia.inventario.domain.politica.PoliticaDeduccionStock;
import com.exodia.inventario.domain.servicio.PoliticaFEFO;
import com.exodia.inventario.excepcion.EntidadNoEncontradaException;
import com.exodia.inventario.excepcion.OperacionInvalidaException;
import com.exodia.inventario.excepcion.StockInsuficienteException;
import com.exodia.inventario.infraestructura.integracion.VentasAdapter;
import com.exodia.inventario.interfaz.dto.peticion.AjusteVentaLineaRequest;
import com.exodia.inventario.interfaz.dto.peticion.CrearAjusteVentaRequest;
import com.exodia.inventario.interfaz.dto.respuesta.AjusteResponse;
import com.exodia.inventario.interfaz.mapeador.AjusteMapeador;
import com.exodia.inventario.repositorio.ajuste.AjusteRepository;
import com.exodia.inventario.repositorio.catalogo.BodegaRepository;
import com.exodia.inventario.repositorio.catalogo.EmpresaRepository;
import com.exodia.inventario.repositorio.catalogo.EstadoContenedorRepository;
import com.exodia.inventario.repositorio.catalogo.TipoAjusteRepository;
import com.exodia.inventario.repositorio.catalogo.UnidadRepository;
import com.exodia.inventario.repositorio.contenedor.ContenedorRepository;
import com.exodia.inventario.repositorio.proyeccion.ContenedorStockProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VentaAjusteServiceImpl implements VentaAjusteService {

    private static final String TIPO_AJUSTE_VENTA_FACTURADA = "VENTA_FACTURADA";

    private final AjusteRepository ajusteRepository;
    private final EmpresaRepository empresaRepository;
    private final BodegaRepository bodegaRepository;
    private final TipoAjusteRepository tipoAjusteRepository;
    private final UnidadRepository unidadRepository;
    private final EstadoContenedorRepository estadoContenedorRepository;
    private final ContenedorRepository contenedorRepository;
    private final OperacionService operacionService;
    private final StockQueryService stockQueryService;
    private final BarcodeService barcodeService;
    private final ConfiguracionEmpresaService configuracionEmpresaService;
    private final PoliticaFEFO politicaFEFO;
    private final PoliticaDeduccionStock politicaDeduccionStock;
    private final VentasAdapter ventasAdapter;
    private final AjusteMapeador ajusteMapeador;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public AjusteResponse crear(Long empresaId, CrearAjusteVentaRequest request) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EntidadNoEncontradaException("Empresa", empresaId));

        Bodega bodega = bodegaRepository.findById(request.bodegaId())
                .filter(b -> b.getEmpresa().getId().equals(empresaId))
                .filter(b -> Boolean.TRUE.equals(b.getActivo()))
                .orElseThrow(() -> new EntidadNoEncontradaException("Bodega", request.bodegaId()));

        TipoAjuste tipoAjuste = tipoAjusteRepository.findByCodigo(TIPO_AJUSTE_VENTA_FACTURADA)
                .orElseThrow(() -> new OperacionInvalidaException(
                        "Tipo de ajuste no encontrado: " + TIPO_AJUSTE_VENTA_FACTURADA));

        ventasAdapter.validarVentaFacturada(
                empresaId,
                request.ventaId(),
                request.lineas().stream()
                        .map(linea -> new VentasAdapter.LineaVentaSolicitud(
                                linea.productoId(),
                                linea.unidadId(),
                                linea.cantidad()))
                        .toList());

        ConfiguracionEmpresa configuracionEmpresa = configuracionEmpresaService.obtenerEntidadOCrear(empresaId);
        String politicaSalida = resolverPoliticaSalida(configuracionEmpresa);
        String numeroAjuste = barcodeService.generarBarcode(empresaId, "VTA");

        Ajuste ajuste = Ajuste.builder()
                .empresa(empresa)
                .numeroAjuste(numeroAjuste)
                .bodega(bodega)
                .tipoAjuste(tipoAjuste)
                .motivo(construirMotivo(request))
                .tipoReferencia(TipoReferencia.VENTA)
                .referenciaId(request.ventaId())
                .build();

        ajuste = ajusteRepository.save(ajuste);

        List<Long> contenedorIdsAfectados = new ArrayList<>();
        List<Long> productoIdsAfectados = new ArrayList<>();

        for (AjusteVentaLineaRequest lineaReq : request.lineas()) {
            Unidad unidad = unidadRepository.findById(lineaReq.unidadId())
                    .filter(u -> u.getEmpresa().getId().equals(empresaId))
                    .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                    .orElseThrow(() -> new EntidadNoEncontradaException("Unidad", lineaReq.unidadId()));

            List<PoliticaFEFO.AsignacionContenedor> asignaciones = resolverAsignaciones(
                    empresaId, bodega.getId(), politicaSalida, lineaReq);

            procesarAsignaciones(
                    ajuste,
                    empresa,
                    bodega.getId(),
                    request.ventaId(),
                    lineaReq,
                    unidad,
                    asignaciones,
                    contenedorIdsAfectados,
                    productoIdsAfectados);
        }

        ajuste = ajusteRepository.save(ajuste);

        List<Long> contenedoresUnicos = contenedorIdsAfectados.stream().distinct().toList();

        log.info("Ajuste por venta {} creado con {} lineas reales y {} contenedores en bodega {}",
                numeroAjuste, ajuste.getLineas().size(), contenedoresUnicos.size(), bodega.getCodigo());

        eventPublisher.publishEvent(new VentaFacturadaAjustadaEvent(
                ajuste.getId(),
                empresaId,
                bodega.getId(),
                request.ventaId(),
                ajuste.getLineas().size(),
                productoIdsAfectados.stream().distinct().toList(),
                contenedoresUnicos));

        return ajusteMapeador.toResponse(ajuste);
    }

    private List<PoliticaFEFO.AsignacionContenedor> resolverAsignaciones(Long empresaId,
                                                                         Long bodegaId,
                                                                         String politicaSalida,
                                                                         AjusteVentaLineaRequest lineaReq) {
        if ("MANUAL".equals(politicaSalida)) {
            if (lineaReq.contenedorId() == null) {
                throw new OperacionInvalidaException(String.format(
                        "Politica MANUAL requiere contenedorId para producto %d",
                        lineaReq.productoId()));
            }
            return List.of(new PoliticaFEFO.AsignacionContenedor(
                    lineaReq.contenedorId(), lineaReq.cantidad()));
        }

        List<ContenedorStockProjection> disponibles = "FIFO".equals(politicaSalida)
                ? stockQueryService.obtenerContenedoresDisponiblesFIFO(
                        empresaId, lineaReq.productoId(), bodegaId)
                : stockQueryService.obtenerContenedoresDisponiblesFEFO(
                        empresaId, lineaReq.productoId(), bodegaId);

        List<PoliticaFEFO.ContenedorConStock> contenedoresConStock = disponibles.stream()
                .map(proyeccion -> new PoliticaFEFO.ContenedorConStock(
                        proyeccion.getContenedorId(),
                        proyeccion.getFechaVencimiento(),
                        null,
                        proyeccion.getCantidadDisponible()))
                .toList();

        List<PoliticaFEFO.AsignacionContenedor> asignaciones = "FIFO".equals(politicaSalida)
                ? politicaFEFO.seleccionarContenedoresEnOrden(contenedoresConStock, lineaReq.cantidad())
                : politicaFEFO.seleccionarContenedores(contenedoresConStock, lineaReq.cantidad());

        BigDecimal totalAsignado = asignaciones.stream()
                .map(PoliticaFEFO.AsignacionContenedor::cantidad)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalAsignado.compareTo(lineaReq.cantidad()) < 0) {
            throw new StockInsuficienteException(
                    lineaReq.productoId(), lineaReq.cantidad(), totalAsignado);
        }

        return asignaciones;
    }

    private void procesarAsignaciones(Ajuste ajuste,
                                      Empresa empresa,
                                      Long bodegaId,
                                      Long ventaId,
                                      AjusteVentaLineaRequest lineaReq,
                                      Unidad unidad,
                                      List<PoliticaFEFO.AsignacionContenedor> asignaciones,
                                      List<Long> contenedorIdsAfectados,
                                      List<Long> productoIdsAfectados) {
        List<Long> idsOrdenados = asignaciones.stream()
                .map(PoliticaFEFO.AsignacionContenedor::contenedorId)
                .distinct()
                .sorted()
                .toList();

        List<Contenedor> contenedoresBloqueados = new ArrayList<>();
        for (Long contenedorId : idsOrdenados) {
            Contenedor contenedor = contenedorRepository.findByIdForUpdate(contenedorId)
                    .orElseThrow(() -> new EntidadNoEncontradaException("Contenedor", contenedorId));
            contenedoresBloqueados.add(contenedor);
        }

        for (PoliticaFEFO.AsignacionContenedor asignacion : asignaciones) {
            Contenedor contenedor = contenedoresBloqueados.stream()
                    .filter(item -> item.getId().equals(asignacion.contenedorId()))
                    .findFirst()
                    .orElseThrow();

            validarContenedor(contenedor, empresa.getId(), bodegaId, lineaReq, unidad);

            BigDecimal stockActual = stockQueryService.obtenerStockContenedor(contenedor.getId());
            BigDecimal stockDisponible = stockQueryService.obtenerStockDisponible(contenedor.getId());

            PoliticaDeduccionStock.ResultadoValidacion resultado = politicaDeduccionStock.evaluar(
                    contenedor.getEstado().getCodigo(), stockDisponible, asignacion.cantidad());

            if (!resultado.valido()) {
                throw new StockInsuficienteException(
                        contenedor.getId(), asignacion.cantidad(), stockDisponible);
            }

            Operacion operacion = operacionService.crearOperacion(
                    contenedor,
                    TipoOperacionCodigo.AJUSTE_VENTA,
                    asignacion.cantidad(),
                    String.format("Venta facturada %d: %s",
                            ventaId, ajuste.getNumeroAjuste()),
                    TipoReferencia.VENTA,
                    ventaId,
                    lineaReq.referenciaLineaId());

            BigDecimal cantidadNueva = stockActual.subtract(asignacion.cantidad());

            if (cantidadNueva.compareTo(BigDecimal.ZERO) <= 0) {
                EstadoContenedor estadoAgotado = estadoContenedorRepository
                        .findByCodigo(EstadoContenedorCodigo.AGOTADO.getCodigo())
                        .orElseThrow(() -> new OperacionInvalidaException(
                                "Estado AGOTADO no encontrado en catalogo"));
                contenedor.setEstado(estadoAgotado);
                contenedorRepository.save(contenedor);
            }

            AjusteLinea linea = AjusteLinea.builder()
                    .ajuste(ajuste)
                    .contenedor(contenedor)
                    .cantidadAnterior(stockActual)
                    .cantidadNueva(cantidadNueva.max(BigDecimal.ZERO))
                    .cantidadAjuste(asignacion.cantidad().negate())
                    .precioAnterior(contenedor.getPrecioUnitario())
                    .precioNuevo(null)
                    .operacion(operacion)
                    .build();

            ajuste.getLineas().add(linea);
            contenedorIdsAfectados.add(contenedor.getId());
            productoIdsAfectados.add(contenedor.getProductoId());
        }
    }

    private void validarContenedor(Contenedor contenedor,
                                   Long empresaId,
                                   Long bodegaId,
                                   AjusteVentaLineaRequest lineaReq,
                                   Unidad unidad) {
        if (!contenedor.getEmpresa().getId().equals(empresaId)) {
            throw new OperacionInvalidaException(String.format(
                    "Contenedor %d no pertenece a la empresa %d",
                    contenedor.getId(), empresaId));
        }
        if (!contenedor.getBodega().getId().equals(bodegaId)) {
            throw new OperacionInvalidaException(String.format(
                    "Contenedor %d esta en bodega %d, no en la bodega %d",
                    contenedor.getId(), contenedor.getBodega().getId(), bodegaId));
        }
        if (!contenedor.getProductoId().equals(lineaReq.productoId())) {
            throw new OperacionInvalidaException(String.format(
                    "Contenedor %d es de producto %d, no coincide con producto %d",
                    contenedor.getId(), contenedor.getProductoId(), lineaReq.productoId()));
        }
        if (!contenedor.getUnidad().getId().equals(unidad.getId())) {
            throw new OperacionInvalidaException(String.format(
                    "Contenedor %d tiene unidad %d, no coincide con unidad %d",
                    contenedor.getId(), contenedor.getUnidad().getId(), unidad.getId()));
        }
    }

    private String resolverPoliticaSalida(ConfiguracionEmpresa configuracionEmpresa) {
        if (configuracionEmpresa == null || configuracionEmpresa.getPoliticaSalida() == null
                || configuracionEmpresa.getPoliticaSalida().isBlank()) {
            return "FEFO";
        }
        return configuracionEmpresa.getPoliticaSalida();
    }

    private String construirMotivo(CrearAjusteVentaRequest request) {
        String motivoBase = "Ajuste por venta facturada " + request.ventaId();
        if (request.comentarios() == null || request.comentarios().isBlank()) {
            return motivoBase;
        }
        return motivoBase + ": " + request.comentarios();
    }
}

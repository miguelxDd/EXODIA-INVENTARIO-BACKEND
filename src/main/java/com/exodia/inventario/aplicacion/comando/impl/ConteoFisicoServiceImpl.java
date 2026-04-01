package com.exodia.inventario.aplicacion.comando.impl;

import com.exodia.inventario.aplicacion.comando.BarcodeService;
import com.exodia.inventario.aplicacion.comando.ConteoFisicoService;
import com.exodia.inventario.aplicacion.comando.OperacionService;
import com.exodia.inventario.aplicacion.consulta.StockQueryService;
import com.exodia.inventario.domain.enums.TipoOperacionCodigo;
import com.exodia.inventario.domain.enums.TipoReferencia;
import com.exodia.inventario.domain.evento.ConteoAplicadoEvent;
import com.exodia.inventario.domain.modelo.ajuste.Ajuste;
import com.exodia.inventario.domain.modelo.ajuste.AjusteLinea;
import com.exodia.inventario.domain.modelo.catalogo.Bodega;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;
import com.exodia.inventario.domain.modelo.catalogo.TipoAjuste;
import com.exodia.inventario.domain.modelo.conteo.ConteoFisico;
import com.exodia.inventario.domain.modelo.conteo.ConteoLinea;
import com.exodia.inventario.domain.modelo.contenedor.Contenedor;
import com.exodia.inventario.domain.modelo.contenedor.Operacion;
import com.exodia.inventario.excepcion.EntidadNoEncontradaException;
import com.exodia.inventario.excepcion.OperacionInvalidaException;
import com.exodia.inventario.interfaz.dto.peticion.CrearConteoFisicoRequest;
import com.exodia.inventario.interfaz.dto.peticion.RegistrarConteoLineaRequest;
import com.exodia.inventario.interfaz.dto.respuesta.ConteoFisicoResponse;
import com.exodia.inventario.interfaz.mapeador.ConteoFisicoMapeador;
import com.exodia.inventario.repositorio.ajuste.AjusteRepository;
import com.exodia.inventario.repositorio.catalogo.BodegaRepository;
import com.exodia.inventario.repositorio.catalogo.EmpresaRepository;
import com.exodia.inventario.repositorio.catalogo.TipoAjusteRepository;
import com.exodia.inventario.repositorio.conteo.ConteoFisicoRepository;
import com.exodia.inventario.repositorio.contenedor.ContenedorRepository;
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
public class ConteoFisicoServiceImpl implements ConteoFisicoService {

    private final ConteoFisicoRepository conteoFisicoRepository;
    private final EmpresaRepository empresaRepository;
    private final BodegaRepository bodegaRepository;
    private final ContenedorRepository contenedorRepository;
    private final AjusteRepository ajusteRepository;
    private final TipoAjusteRepository tipoAjusteRepository;
    private final OperacionService operacionService;
    private final StockQueryService stockQueryService;
    private final BarcodeService barcodeService;
    private final ConteoFisicoMapeador conteoFisicoMapeador;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public ConteoFisicoResponse crear(Long empresaId, CrearConteoFisicoRequest request) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EntidadNoEncontradaException("Empresa", empresaId));

        Bodega bodega = bodegaRepository.findById(request.bodegaId())
                .filter(b -> b.getEmpresa().getId().equals(empresaId))
                .filter(b -> Boolean.TRUE.equals(b.getActivo()))
                .orElseThrow(() -> new EntidadNoEncontradaException("Bodega", request.bodegaId()));

        String numeroConteo = barcodeService.generarBarcode(empresaId, "CNT");

        ConteoFisico conteo = ConteoFisico.builder()
                .empresa(empresa)
                .numeroConteo(numeroConteo)
                .bodega(bodega)
                .comentarios(request.comentarios())
                .build();

        conteo = conteoFisicoRepository.save(conteo);

        log.info("Conteo fisico {} creado en bodega {} para empresa {}",
                numeroConteo, bodega.getCodigo(), empresaId);

        return conteoFisicoMapeador.toResponse(conteo);
    }

    @Override
    @Transactional
    public ConteoFisicoResponse registrarLinea(Long empresaId, Long conteoId,
                                                RegistrarConteoLineaRequest request) {
        ConteoFisico conteo = conteoFisicoRepository.findById(conteoId)
                .filter(c -> c.getEmpresa().getId().equals(empresaId))
                .orElseThrow(() -> new EntidadNoEncontradaException("ConteoFisico", conteoId));

        if (!"EN_PROGRESO".equals(conteo.getEstado())) {
            throw new OperacionInvalidaException(
                    "Solo se pueden registrar lineas en conteos EN_PROGRESO, actual: " + conteo.getEstado());
        }

        Long bodegaIdConteo = conteo.getBodega().getId();

        Contenedor contenedor = contenedorRepository.findById(request.contenedorId())
                .filter(c -> c.getEmpresa().getId().equals(empresaId))
                .filter(c -> c.getBodega().getId().equals(bodegaIdConteo))
                .orElseThrow(() -> new EntidadNoEncontradaException("Contenedor", request.contenedorId()));

        BigDecimal cantidadSistema = stockQueryService.obtenerStockContenedor(contenedor.getId());

        // Verificar si ya existe linea para este contenedor
        boolean yaRegistrado = conteo.getLineas().stream()
                .anyMatch(l -> l.getContenedor().getId().equals(request.contenedorId()));
        if (yaRegistrado) {
            throw new OperacionInvalidaException(
                    "El contenedor " + request.contenedorId() + " ya tiene una linea en este conteo");
        }

        BigDecimal diferencia = request.cantidadContada().subtract(cantidadSistema);

        ConteoLinea linea = ConteoLinea.builder()
                .conteoFisico(conteo)
                .contenedor(contenedor)
                .cantidadSistema(cantidadSistema)
                .cantidadContada(request.cantidadContada())
                .diferencia(diferencia)
                .build();

        conteo.getLineas().add(linea);
        conteo = conteoFisicoRepository.save(conteo);

        log.info("Linea registrada en conteo {}: contenedor={}, sistema={}, contada={}, diferencia={}",
                conteo.getNumeroConteo(), contenedor.getCodigoBarras(),
                cantidadSistema, request.cantidadContada(), diferencia);

        return conteoFisicoMapeador.toResponse(conteo);
    }

    @Override
    @Transactional
    public ConteoFisicoResponse aplicar(Long empresaId, Long conteoId) {
        ConteoFisico conteo = conteoFisicoRepository.findById(conteoId)
                .filter(c -> c.getEmpresa().getId().equals(empresaId))
                .orElseThrow(() -> new EntidadNoEncontradaException("ConteoFisico", conteoId));

        if (!"EN_PROGRESO".equals(conteo.getEstado())) {
            throw new OperacionInvalidaException(
                    "Solo se pueden aplicar conteos EN_PROGRESO, actual: " + conteo.getEstado());
        }

        if (conteo.getLineas().isEmpty()) {
            throw new OperacionInvalidaException("El conteo no tiene lineas registradas");
        }

        // Crear ajuste automático con las diferencias
        String numeroAjuste = barcodeService.generarBarcode(empresaId, "AJC");

        TipoAjuste tipoAjusteDiferenciaConteo = tipoAjusteRepository
                .findByCodigo("DIFERENCIA_CONTEO")
                .orElseThrow(() -> new OperacionInvalidaException(
                        "TipoAjuste DIFERENCIA_CONTEO no encontrado en catalogo"));

        Ajuste ajuste = Ajuste.builder()
                .empresa(conteo.getEmpresa())
                .numeroAjuste(numeroAjuste)
                .bodega(conteo.getBodega())
                .tipoAjuste(tipoAjusteDiferenciaConteo)
                .motivo("Ajuste por conteo fisico " + conteo.getNumeroConteo())
                .tipoReferencia(TipoReferencia.CONTEO_FISICO)
                .build();

        ajuste = ajusteRepository.save(ajuste);

        List<Long> ajusteIds = new ArrayList<>();
        int ajustesPositivos = 0;
        int ajustesNegativos = 0;

        // Ordenar IDs para locks secuenciales y evitar deadlocks
        List<ConteoLinea> lineasConDiferencia = conteo.getLineas().stream()
                .filter(l -> l.getDiferencia().compareTo(BigDecimal.ZERO) != 0)
                .sorted((a, b) -> a.getContenedor().getId().compareTo(b.getContenedor().getId()))
                .toList();

        for (ConteoLinea linea : lineasConDiferencia) {
            Contenedor contenedor = contenedorRepository.findByIdForUpdate(linea.getContenedor().getId())
                    .orElseThrow(() -> new EntidadNoEncontradaException(
                            "Contenedor", linea.getContenedor().getId()));

            // Recalcular diferencia con lock
            BigDecimal stockActual = stockQueryService.obtenerStockContenedor(contenedor.getId());
            BigDecimal cantidadAjuste = linea.getCantidadContada().subtract(stockActual);

            if (cantidadAjuste.compareTo(BigDecimal.ZERO) == 0) {
                linea.setAplicado(true);
                continue;
            }

            TipoOperacionCodigo tipoCodigo = cantidadAjuste.compareTo(BigDecimal.ZERO) > 0
                    ? TipoOperacionCodigo.CONTEO_POSITIVO
                    : TipoOperacionCodigo.CONTEO_NEGATIVO;

            Operacion operacion = operacionService.crearOperacion(
                    contenedor,
                    tipoCodigo,
                    cantidadAjuste.abs(),
                    "Conteo fisico " + conteo.getNumeroConteo(),
                    TipoReferencia.CONTEO_FISICO,
                    conteo.getId(),
                    linea.getId());

            AjusteLinea ajusteLinea = AjusteLinea.builder()
                    .ajuste(ajuste)
                    .contenedor(contenedor)
                    .cantidadAnterior(stockActual)
                    .cantidadNueva(linea.getCantidadContada())
                    .cantidadAjuste(cantidadAjuste)
                    .operacion(operacion)
                    .build();

            ajuste.getLineas().add(ajusteLinea);
            linea.setAplicado(true);

            ajusteIds.add(ajuste.getId());
            if (cantidadAjuste.compareTo(BigDecimal.ZERO) > 0) {
                ajustesPositivos++;
            } else {
                ajustesNegativos++;
            }
        }

        ajuste = ajusteRepository.save(ajuste);
        conteo.setAjusteGenerado(ajuste);
        conteo.setEstado("APLICADO");
        conteo = conteoFisicoRepository.save(conteo);

        log.info("Conteo fisico {} aplicado: {} ajustes positivos, {} ajustes negativos",
                conteo.getNumeroConteo(), ajustesPositivos, ajustesNegativos);

        eventPublisher.publishEvent(new ConteoAplicadoEvent(
                conteo.getId(), empresaId, conteo.getBodega().getId(),
                ajusteIds, ajustesPositivos, ajustesNegativos));

        return conteoFisicoMapeador.toResponse(conteo);
    }

    @Override
    @Transactional(readOnly = true)
    public ConteoFisicoResponse obtenerPorId(Long empresaId, Long conteoId) {
        ConteoFisico conteo = conteoFisicoRepository.findById(conteoId)
                .filter(c -> c.getEmpresa().getId().equals(empresaId))
                .orElseThrow(() -> new EntidadNoEncontradaException("ConteoFisico", conteoId));
        return conteoFisicoMapeador.toResponse(conteo);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConteoFisicoResponse> listarPorEmpresa(Long empresaId, Pageable pageable) {
        return conteoFisicoRepository.findByEmpresaIdOrderByCreadoEnDesc(empresaId, pageable)
                .map(conteoFisicoMapeador::toResponse);
    }

    @Override
    @Transactional
    public ConteoFisicoResponse cancelar(Long empresaId, Long conteoId) {
        ConteoFisico conteo = conteoFisicoRepository.findById(conteoId)
                .filter(c -> c.getEmpresa().getId().equals(empresaId))
                .orElseThrow(() -> new EntidadNoEncontradaException("ConteoFisico", conteoId));

        if (!"EN_PROGRESO".equals(conteo.getEstado())) {
            throw new OperacionInvalidaException(
                    "Solo se pueden cancelar conteos EN_PROGRESO, actual: " + conteo.getEstado());
        }

        conteo.setEstado("CANCELADO");
        conteo = conteoFisicoRepository.save(conteo);

        log.info("Conteo fisico {} cancelado", conteo.getNumeroConteo());

        return conteoFisicoMapeador.toResponse(conteo);
    }
}

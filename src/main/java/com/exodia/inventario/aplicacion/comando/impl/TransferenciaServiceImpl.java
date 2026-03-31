package com.exodia.inventario.aplicacion.comando.impl;

import com.exodia.inventario.aplicacion.comando.BarcodeService;
import com.exodia.inventario.aplicacion.comando.ConfiguracionEmpresaService;
import com.exodia.inventario.aplicacion.comando.OperacionService;
import com.exodia.inventario.aplicacion.comando.TransferenciaService;
import com.exodia.inventario.aplicacion.consulta.StockQueryService;
import com.exodia.inventario.domain.enums.EstadoContenedorCodigo;
import com.exodia.inventario.domain.enums.EstadoTransferenciaCodigo;
import com.exodia.inventario.domain.enums.TipoOperacionCodigo;
import com.exodia.inventario.domain.enums.TipoReferencia;
import com.exodia.inventario.domain.enums.TipoTransferencia;
import com.exodia.inventario.domain.evento.TransferenciaDespachadaEvent;
import com.exodia.inventario.domain.evento.TransferenciaRecibidaEvent;
import com.exodia.inventario.domain.modelo.catalogo.Bodega;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;
import com.exodia.inventario.domain.modelo.catalogo.EstadoContenedor;
import com.exodia.inventario.domain.modelo.catalogo.EstadoTransferencia;
import com.exodia.inventario.domain.modelo.catalogo.Ubicacion;
import com.exodia.inventario.domain.modelo.catalogo.Unidad;
import com.exodia.inventario.domain.modelo.contenedor.Contenedor;
import com.exodia.inventario.domain.modelo.contenedor.Operacion;
import com.exodia.inventario.domain.modelo.extension.ConfiguracionEmpresa;
import com.exodia.inventario.domain.modelo.transferencia.Transferencia;
import com.exodia.inventario.domain.modelo.transferencia.TransferenciaContenedor;
import com.exodia.inventario.domain.modelo.transferencia.TransferenciaContenedorEntrada;
import com.exodia.inventario.domain.modelo.transferencia.TransferenciaLinea;
import com.exodia.inventario.domain.politica.PoliticaDeduccionStock;
import com.exodia.inventario.domain.servicio.PoliticaFEFO;
import com.exodia.inventario.domain.servicio.ValidadorEstadoTransferencia;
import com.exodia.inventario.excepcion.EntidadNoEncontradaException;
import com.exodia.inventario.excepcion.EstadoTransferenciaException;
import com.exodia.inventario.excepcion.OperacionInvalidaException;
import com.exodia.inventario.excepcion.StockInsuficienteException;
import com.exodia.inventario.interfaz.dto.peticion.CrearTransferenciaRequest;
import com.exodia.inventario.interfaz.dto.peticion.RecibirTransferenciaRequest;
import com.exodia.inventario.interfaz.dto.peticion.TransferenciaLineaRequest;
import com.exodia.inventario.interfaz.dto.respuesta.TransferenciaResponse;
import com.exodia.inventario.interfaz.mapeador.TransferenciaMapeador;
import com.exodia.inventario.repositorio.catalogo.BodegaRepository;
import com.exodia.inventario.repositorio.catalogo.EmpresaRepository;
import com.exodia.inventario.repositorio.catalogo.EstadoContenedorRepository;
import com.exodia.inventario.repositorio.catalogo.EstadoTransferenciaRepository;
import com.exodia.inventario.repositorio.catalogo.UbicacionRepository;
import com.exodia.inventario.repositorio.catalogo.UnidadRepository;
import com.exodia.inventario.repositorio.contenedor.ContenedorRepository;
import com.exodia.inventario.repositorio.proyeccion.ContenedorStockProjection;
import com.exodia.inventario.repositorio.transferencia.TransferenciaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferenciaServiceImpl implements TransferenciaService {

    private final TransferenciaRepository transferenciaRepository;
    private final EmpresaRepository empresaRepository;
    private final BodegaRepository bodegaRepository;
    private final UbicacionRepository ubicacionRepository;
    private final UnidadRepository unidadRepository;
    private final ContenedorRepository contenedorRepository;
    private final EstadoTransferenciaRepository estadoTransferenciaRepository;
    private final EstadoContenedorRepository estadoContenedorRepository;
    private final OperacionService operacionService;
    private final StockQueryService stockQueryService;
    private final BarcodeService barcodeService;
    private final ConfiguracionEmpresaService configuracionEmpresaService;
    private final ValidadorEstadoTransferencia validadorEstado;
    private final PoliticaFEFO politicaFEFO;
    private final PoliticaDeduccionStock politicaDeduccionStock;
    private final TransferenciaMapeador transferenciaMapeador;
    private final ApplicationEventPublisher eventPublisher;

    // ── Crear (BORRADOR) ─────────────────────────────────────────────────────

    @Override
    @Transactional
    public TransferenciaResponse crear(Long empresaId, CrearTransferenciaRequest request) {
        Empresa empresa = buscarEmpresa(empresaId);

        Bodega bodegaOrigen = buscarBodega(empresaId, request.bodegaOrigenId());
        Bodega bodegaDestino = buscarBodega(empresaId, request.bodegaDestinoId());

        if (bodegaOrigen.getId().equals(bodegaDestino.getId())) {
            throw new OperacionInvalidaException("La bodega origen y destino no pueden ser la misma");
        }

        EstadoTransferencia estadoBorrador = buscarEstadoTransferencia(
                EstadoTransferenciaCodigo.BORRADOR);

        String numero = barcodeService.generarBarcode(empresaId, "TRF");

        Transferencia transferencia = Transferencia.builder()
                .empresa(empresa)
                .numeroTransferencia(numero)
                .tipoTransferencia(TipoTransferencia.valueOf(request.tipoTransferencia()))
                .bodegaOrigen(bodegaOrigen)
                .bodegaDestino(bodegaDestino)
                .estadoTransferencia(estadoBorrador)
                .comentarios(request.comentarios())
                .build();

        // Crear lineas
        for (TransferenciaLineaRequest lineaReq : request.lineas()) {
            Unidad unidad = unidadRepository.findById(lineaReq.unidadId())
                    .filter(u -> u.getEmpresa().getId().equals(empresaId))
                    .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                    .orElseThrow(() -> new EntidadNoEncontradaException("Unidad", lineaReq.unidadId()));

            TransferenciaLinea linea = TransferenciaLinea.builder()
                    .transferencia(transferencia)
                    .productoId(lineaReq.productoId())
                    .unidad(unidad)
                    .contenedorId(lineaReq.contenedorId())
                    .cantidadSolicitada(lineaReq.cantidadSolicitada())
                    .build();
            transferencia.getLineas().add(linea);
        }

        transferencia = transferenciaRepository.save(transferencia);
        log.info("Transferencia {} creada: {} -> {} ({} lineas)",
                numero, bodegaOrigen.getCodigo(), bodegaDestino.getCodigo(),
                request.lineas().size());

        return transferenciaMapeador.toResponse(transferencia);
    }

    // ── Confirmar (BORRADOR -> CONFIRMADO) ───────────────────────────────────

    @Override
    @Transactional
    public TransferenciaResponse confirmar(Long empresaId, Long transferenciaId) {
        Transferencia transferencia = buscarTransferencia(empresaId, transferenciaId);

        validarYTransicionar(transferencia, EstadoTransferenciaCodigo.CONFIRMADO);

        if (transferencia.getLineas().isEmpty()) {
            throw new OperacionInvalidaException(
                    "No se puede confirmar una transferencia sin lineas");
        }

        transferencia = transferenciaRepository.save(transferencia);
        log.info("Transferencia {} confirmada", transferencia.getNumeroTransferencia());

        return transferenciaMapeador.toResponse(transferencia);
    }

    // ── Despachar (CONFIRMADO -> DESPACHADO -> EN_TRANSITO) ──────────────────

    @Override
    @Transactional
    public TransferenciaResponse despachar(Long empresaId, Long transferenciaId) {
        Transferencia transferencia = buscarTransferencia(empresaId, transferenciaId);

        validarYTransicionar(transferencia, EstadoTransferenciaCodigo.DESPACHADO);

        EstadoContenedor estadoEnTransito = estadoContenedorRepository
                .findByCodigo(EstadoContenedorCodigo.EN_TRANSITO.getCodigo())
                .orElseThrow(() -> new OperacionInvalidaException(
                        "Estado EN_TRANSITO no encontrado en catalogo"));

        List<Long> contenedorIdsAfectados = new ArrayList<>();

        if (transferencia.getTipoTransferencia() == TipoTransferencia.POR_PRODUCTO) {
            // Resolver contenedores via FEFO para cada linea
            for (TransferenciaLinea linea : transferencia.getLineas()) {
                List<TransferenciaContenedor> asignados = resolverContenedoresFEFO(
                        transferencia, linea, empresaId, estadoEnTransito);

                BigDecimal totalDespachado = BigDecimal.ZERO;
                for (TransferenciaContenedor tc : asignados) {
                    transferencia.getContenedores().add(tc);
                    contenedorIdsAfectados.add(tc.getContenedor().getId());
                    totalDespachado = totalDespachado.add(tc.getCantidad());
                }
                linea.setCantidadDespachada(totalDespachado);
            }
        } else {
            // POR_CONTENEDOR: los contenedores se indican explicitamente en cada linea
            despacharContenedoresExplicitos(transferencia, empresaId, estadoEnTransito,
                    contenedorIdsAfectados);
        }

        // Transicionar a EN_TRANSITO
        EstadoTransferencia estadoTransito = buscarEstadoTransferencia(
                EstadoTransferenciaCodigo.EN_TRANSITO);
        transferencia.setEstadoTransferencia(estadoTransito);
        transferencia.setFechaDespacho(OffsetDateTime.now());

        transferencia = transferenciaRepository.save(transferencia);
        log.info("Transferencia {} despachada con {} contenedores",
                transferencia.getNumeroTransferencia(), contenedorIdsAfectados.size());

        eventPublisher.publishEvent(new TransferenciaDespachadaEvent(
                transferencia.getId(), empresaId,
                transferencia.getBodegaOrigen().getId(),
                transferencia.getBodegaDestino().getId(),
                contenedorIdsAfectados));

        return transferenciaMapeador.toResponse(transferencia);
    }

    // ── Recibir (EN_TRANSITO -> RECIBIDO_PARCIAL / RECIBIDO_COMPLETO) ────────

    @Override
    @Transactional
    public TransferenciaResponse recibir(Long empresaId, Long transferenciaId,
                                          RecibirTransferenciaRequest request) {
        Transferencia transferencia = buscarTransferencia(empresaId, transferenciaId);

        EstadoTransferenciaCodigo estadoActual = EstadoTransferenciaCodigo.valueOf(
                transferencia.getEstadoTransferencia().getCodigo());

        // Validar que esta en estado que permite recepcion
        if (estadoActual != EstadoTransferenciaCodigo.EN_TRANSITO
                && estadoActual != EstadoTransferenciaCodigo.RECIBIDO_PARCIAL) {
            throw new EstadoTransferenciaException(
                    estadoActual.getCodigo(), "RECIBIDO");
        }

        Long bodegaDestinoId = transferencia.getBodegaDestino().getId();

        Ubicacion ubicacionDestino = ubicacionRepository.findById(request.ubicacionDestinoId())
                .filter(u -> u.getBodega().getId().equals(bodegaDestinoId))
                .orElseThrow(() -> new EntidadNoEncontradaException(
                        "Ubicacion", request.ubicacionDestinoId()));

        EstadoContenedor estadoDisponible = estadoContenedorRepository
                .findByCodigo(EstadoContenedorCodigo.DISPONIBLE.getCodigo())
                .orElseThrow(() -> new OperacionInvalidaException(
                        "Estado DISPONIBLE no encontrado en catalogo"));

        List<Long> contenedorIdsRecibidos = new ArrayList<>();

        for (RecibirTransferenciaRequest.RecepcionContenedorRequest recepcion : request.contenedores()) {
            TransferenciaContenedor tc = transferencia.getContenedores().stream()
                    .filter(c -> c.getContenedor().getId().equals(recepcion.contenedorId()))
                    .filter(c -> !Boolean.TRUE.equals(c.getRecibido()))
                    .findFirst()
                    .orElseThrow(() -> new OperacionInvalidaException(
                            "Contenedor " + recepcion.contenedorId()
                                    + " no encontrado en transferencia o ya fue recibido"));

            Contenedor contenedorOrigen = tc.getContenedor();
            BigDecimal pendientePorRecibir = tc.getCantidad().subtract(tc.getCantidadRecibida());
            BigDecimal cantidadRecibida = recepcion.cantidadRecibida() != null
                    ? recepcion.cantidadRecibida() : pendientePorRecibir;

            // Validar que cantidadRecibida no exceda lo pendiente por recibir
            if (cantidadRecibida.compareTo(pendientePorRecibir) > 0) {
                throw new OperacionInvalidaException(String.format(
                        "Cantidad recibida (%s) excede el pendiente (%s) para contenedor %d",
                        cantidadRecibida, pendientePorRecibir, contenedorOrigen.getId()));
            }
            if (cantidadRecibida.compareTo(BigDecimal.ZERO) <= 0) {
                throw new OperacionInvalidaException(String.format(
                        "Cantidad recibida debe ser mayor a cero para contenedor %d",
                        contenedorOrigen.getId()));
            }

            // Determinar si el contenedor fue totalmente tomado (stock remanente = 0 en origen)
            BigDecimal stockRemanente = stockQueryService.obtenerStockContenedor(contenedorOrigen.getId());
            boolean contenedorVacio = stockRemanente.compareTo(BigDecimal.ZERO) <= 0;

            Contenedor contenedorDestino;
            if (contenedorVacio) {
                // Contenedor completamente tomado: moverlo a destino
                contenedorOrigen.setBodega(transferencia.getBodegaDestino());
                contenedorOrigen.setUbicacion(ubicacionDestino);
                contenedorOrigen.setEstado(estadoDisponible);
                contenedorRepository.save(contenedorOrigen);
                contenedorDestino = contenedorOrigen;
            } else {
                // Contenedor parcialmente tomado: crear nuevo contenedor en destino
                String nuevoBarcode = barcodeService.generarBarcode(empresaId);
                contenedorDestino = Contenedor.builder()
                        .empresa(contenedorOrigen.getEmpresa())
                        .codigoBarras(nuevoBarcode)
                        .productoId(contenedorOrigen.getProductoId())
                        .proveedorId(contenedorOrigen.getProveedorId())
                        .unidad(contenedorOrigen.getUnidad())
                        .bodega(transferencia.getBodegaDestino())
                        .ubicacion(ubicacionDestino)
                        .precioUnitario(contenedorOrigen.getPrecioUnitario())
                        .lote(contenedorOrigen.getLote())
                        .numeroLote(contenedorOrigen.getNumeroLote())
                        .fechaVencimiento(contenedorOrigen.getFechaVencimiento())
                        .estado(estadoDisponible)
                        .build();
                contenedorDestino = contenedorRepository.save(contenedorDestino);
            }

            // Crear operacion ENTRADA_TRANSFERENCIA en el contenedor destino
            Operacion opEntrada = operacionService.crearOperacion(
                    contenedorDestino,
                    TipoOperacionCodigo.ENTRADA_TRANSFERENCIA,
                    cantidadRecibida,
                    "Recepcion transferencia " + transferencia.getNumeroTransferencia(),
                    TipoReferencia.TRANSFERENCIA,
                    transferencia.getId(),
                    tc.getId());

            // Registrar entrada en historial de recepciones parciales
            TransferenciaContenedorEntrada entrada = TransferenciaContenedorEntrada.builder()
                    .transferenciaContenedor(tc)
                    .operacionEntrada(opEntrada)
                    .cantidadRecibida(cantidadRecibida)
                    .contenedorDestino(contenedorDestino)
                    .build();
            tc.getEntradas().add(entrada);

            // Acumular cantidad recibida; marcar recibido solo si se cubrió todo
            tc.setCantidadRecibida(tc.getCantidadRecibida().add(cantidadRecibida));
            if (tc.getCantidadRecibida().compareTo(tc.getCantidad()) >= 0) {
                tc.setRecibido(true);
            }
            tc.setOperacionEntrada(opEntrada);
            contenedorIdsRecibidos.add(contenedorDestino.getId());

            // Actualizar cantidad recibida en la linea correspondiente
            actualizarCantidadRecibidaEnLinea(transferencia, tc.getTransferenciaLineaId(),
                    contenedorOrigen.getProductoId(), cantidadRecibida);
        }

        // Determinar si recepcion es completa o parcial
        boolean todosRecibidos = transferencia.getContenedores().stream()
                .allMatch(c -> Boolean.TRUE.equals(c.getRecibido()));

        EstadoTransferenciaCodigo nuevoEstado = todosRecibidos
                ? EstadoTransferenciaCodigo.RECIBIDO_COMPLETO
                : EstadoTransferenciaCodigo.RECIBIDO_PARCIAL;

        EstadoTransferencia estadoNuevo = buscarEstadoTransferencia(nuevoEstado);
        transferencia.setEstadoTransferencia(estadoNuevo);

        if (todosRecibidos) {
            transferencia.setFechaRecepcion(OffsetDateTime.now());
        }

        transferencia = transferenciaRepository.save(transferencia);
        log.info("Transferencia {} recibida ({}): {} contenedores",
                transferencia.getNumeroTransferencia(), nuevoEstado.getCodigo(),
                contenedorIdsRecibidos.size());

        eventPublisher.publishEvent(new TransferenciaRecibidaEvent(
                transferencia.getId(), empresaId,
                transferencia.getBodegaDestino().getId(),
                contenedorIdsRecibidos, todosRecibidos));

        return transferenciaMapeador.toResponse(transferencia);
    }

    // ── Cancelar (BORRADOR/CONFIRMADO -> CANCELADO) ──────────────────────────

    @Override
    @Transactional
    public TransferenciaResponse cancelar(Long empresaId, Long transferenciaId) {
        Transferencia transferencia = buscarTransferencia(empresaId, transferenciaId);

        validarYTransicionar(transferencia, EstadoTransferenciaCodigo.CANCELADO);

        transferencia = transferenciaRepository.save(transferencia);
        log.info("Transferencia {} cancelada", transferencia.getNumeroTransferencia());

        return transferenciaMapeador.toResponse(transferencia);
    }

    // ── Consultas ────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public TransferenciaResponse obtenerPorId(Long empresaId, Long transferenciaId) {
        Transferencia transferencia = buscarTransferencia(empresaId, transferenciaId);
        return transferenciaMapeador.toResponse(transferencia);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransferenciaResponse> listarPorEmpresa(Long empresaId, Pageable pageable) {
        return transferenciaRepository.findByEmpresaIdOrderByCreadoEnDesc(empresaId, pageable)
                .map(transferenciaMapeador::toResponse);
    }

    // ── Metodos privados ─────────────────────────────────────────────────────

    /**
     * Resuelve contenedores segun politica configurada (FEFO/FIFO) para una linea POR_PRODUCTO.
     * Adquiere locks en orden ascendente por ID para evitar deadlocks.
     */
    private List<TransferenciaContenedor> resolverContenedoresFEFO(
            Transferencia transferencia, TransferenciaLinea linea,
            Long empresaId, EstadoContenedor estadoEnTransito) {

        // Consultar politica de salida configurada
        ConfiguracionEmpresa configEmpresa = configuracionEmpresaService.obtenerEntidadOCrear(empresaId);
        String politica = configEmpresa.getPoliticaSalida();

        // Obtener contenedores disponibles segun politica
        List<ContenedorStockProjection> disponibles = "FIFO".equals(politica)
                ? stockQueryService.obtenerContenedoresDisponiblesFIFO(
                        empresaId, linea.getProductoId(),
                        transferencia.getBodegaOrigen().getId())
                : stockQueryService.obtenerContenedoresDisponiblesFEFO(
                        empresaId, linea.getProductoId(),
                        transferencia.getBodegaOrigen().getId());

        // Convertir a formato PoliticaFEFO
        List<PoliticaFEFO.ContenedorConStock> contenedoresConStock = disponibles.stream()
                .map(p -> new PoliticaFEFO.ContenedorConStock(
                        p.getContenedorId(), p.getFechaVencimiento(),
                        null, p.getCantidadDisponible()))
                .toList();

        // Seleccionar contenedores para cubrir la cantidad solicitada
        List<PoliticaFEFO.AsignacionContenedor> asignaciones = politicaFEFO
                .seleccionarContenedores(contenedoresConStock, linea.getCantidadSolicitada());

        if (asignaciones.isEmpty()) {
            throw new OperacionInvalidaException(String.format(
                    "No hay contenedores disponibles para producto %d en bodega %s",
                    linea.getProductoId(), transferencia.getBodegaOrigen().getCodigo()));
        }

        // Verificar que se cubre la cantidad total
        BigDecimal totalAsignado = asignaciones.stream()
                .map(PoliticaFEFO.AsignacionContenedor::cantidad)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalAsignado.compareTo(linea.getCantidadSolicitada()) < 0) {
            throw new StockInsuficienteException(
                    asignaciones.getFirst().contenedorId(),
                    linea.getCantidadSolicitada(), totalAsignado);
        }

        // Lock pesimista en orden ascendente por ID (previene deadlocks)
        List<Long> idsOrdenados = asignaciones.stream()
                .map(PoliticaFEFO.AsignacionContenedor::contenedorId)
                .sorted()
                .toList();

        for (Long id : idsOrdenados) {
            contenedorRepository.findByIdForUpdate(id);
        }

        // Crear operaciones de salida y TransferenciaContenedor
        List<TransferenciaContenedor> resultado = new ArrayList<>();

        for (PoliticaFEFO.AsignacionContenedor asignacion : asignaciones) {
            Contenedor contenedor = contenedorRepository.findById(asignacion.contenedorId())
                    .orElseThrow(() -> new EntidadNoEncontradaException(
                            "Contenedor", asignacion.contenedorId()));

            // Validar stock disponible con lock adquirido
            BigDecimal stockDisponible = stockQueryService.obtenerStockDisponible(
                    contenedor.getId());

            PoliticaDeduccionStock.ResultadoValidacion validacion = politicaDeduccionStock
                    .evaluar(contenedor.getEstado().getCodigo(),
                            stockDisponible, asignacion.cantidad());

            if (!validacion.valido()) {
                throw new StockInsuficienteException(
                        contenedor.getId(), asignacion.cantidad(), stockDisponible);
            }

            // Crear operacion SALIDA_TRANSFERENCIA
            Operacion opSalida = operacionService.crearOperacion(
                    contenedor,
                    TipoOperacionCodigo.SALIDA_TRANSFERENCIA,
                    asignacion.cantidad(),
                    "Despacho transferencia " + transferencia.getNumeroTransferencia(),
                    TipoReferencia.TRANSFERENCIA,
                    transferencia.getId(),
                    linea.getId());

            // Solo cambiar estado a EN_TRANSITO si el contenedor queda sin stock
            BigDecimal stockRestante = stockDisponible.subtract(asignacion.cantidad());
            if (stockRestante.compareTo(BigDecimal.ZERO) <= 0) {
                contenedor.setEstado(estadoEnTransito);
            }
            contenedorRepository.save(contenedor);

            TransferenciaContenedor tc = TransferenciaContenedor.builder()
                    .transferencia(transferencia)
                    .contenedor(contenedor)
                    .transferenciaLineaId(linea.getId())
                    .cantidad(asignacion.cantidad())
                    .operacionSalida(opSalida)
                    .build();
            resultado.add(tc);
        }

        return resultado;
    }

    /**
     * Despacha contenedores indicados explicitamente (POR_CONTENEDOR).
     * Cada linea debe tener un contenedorId asignado.
     * Locks en orden ascendente por ID para evitar deadlocks.
     */
    private void despacharContenedoresExplicitos(Transferencia transferencia, Long empresaId,
                                                  EstadoContenedor estadoEnTransito,
                                                  List<Long> contenedorIdsAfectados) {
        // Validar que todas las lineas tienen contenedorId
        for (TransferenciaLinea linea : transferencia.getLineas()) {
            if (linea.getContenedorId() == null) {
                throw new OperacionInvalidaException(String.format(
                        "Linea de producto %d no tiene contenedorId asignado (requerido para POR_CONTENEDOR)",
                        linea.getProductoId()));
            }
        }

        // Recopilar IDs de contenedor y adquirir locks en orden ascendente
        List<Long> idsOrdenados = transferencia.getLineas().stream()
                .map(TransferenciaLinea::getContenedorId)
                .distinct()
                .sorted()
                .toList();

        for (Long id : idsOrdenados) {
            contenedorRepository.findByIdForUpdate(id);
        }

        // Procesar cada linea con su contenedor especifico
        for (TransferenciaLinea linea : transferencia.getLineas()) {
            Contenedor contenedor = contenedorRepository.findById(linea.getContenedorId())
                    .filter(c -> c.getEmpresa().getId().equals(empresaId))
                    .orElseThrow(() -> new EntidadNoEncontradaException(
                            "Contenedor", linea.getContenedorId()));

            // Validar que el contenedor pertenece a la bodega origen
            if (!contenedor.getBodega().getId().equals(transferencia.getBodegaOrigen().getId())) {
                throw new OperacionInvalidaException(String.format(
                        "Contenedor %d no pertenece a la bodega origen %s",
                        contenedor.getId(), transferencia.getBodegaOrigen().getCodigo()));
            }

            BigDecimal stockDisponible = stockQueryService.obtenerStockDisponible(
                    contenedor.getId());

            PoliticaDeduccionStock.ResultadoValidacion validacion = politicaDeduccionStock
                    .evaluar(contenedor.getEstado().getCodigo(),
                            stockDisponible, linea.getCantidadSolicitada());

            if (!validacion.valido()) {
                throw new StockInsuficienteException(
                        contenedor.getId(), linea.getCantidadSolicitada(), stockDisponible);
            }

            Operacion opSalida = operacionService.crearOperacion(
                    contenedor,
                    TipoOperacionCodigo.SALIDA_TRANSFERENCIA,
                    linea.getCantidadSolicitada(),
                    "Despacho transferencia " + transferencia.getNumeroTransferencia(),
                    TipoReferencia.TRANSFERENCIA,
                    transferencia.getId(),
                    linea.getId());

            // Solo cambiar estado a EN_TRANSITO si el contenedor queda sin stock
            BigDecimal stockRestante = stockDisponible.subtract(linea.getCantidadSolicitada());
            if (stockRestante.compareTo(BigDecimal.ZERO) <= 0) {
                contenedor.setEstado(estadoEnTransito);
            }
            contenedorRepository.save(contenedor);

            TransferenciaContenedor tc = TransferenciaContenedor.builder()
                    .transferencia(transferencia)
                    .contenedor(contenedor)
                    .transferenciaLineaId(linea.getId())
                    .cantidad(linea.getCantidadSolicitada())
                    .operacionSalida(opSalida)
                    .build();
            transferencia.getContenedores().add(tc);

            contenedorIdsAfectados.add(contenedor.getId());
            linea.setCantidadDespachada(linea.getCantidadSolicitada());
        }
    }

    private void actualizarCantidadRecibidaEnLinea(Transferencia transferencia,
                                                    Long transferenciaLineaId,
                                                    Long productoId,
                                                    BigDecimal cantidadRecibida) {
        // Buscar primero por lineaId exacto, fallback a productoId para datos legacy
        TransferenciaLinea linea = null;
        if (transferenciaLineaId != null) {
            linea = transferencia.getLineas().stream()
                    .filter(l -> l.getId().equals(transferenciaLineaId))
                    .findFirst()
                    .orElse(null);
        }
        if (linea == null) {
            linea = transferencia.getLineas().stream()
                    .filter(l -> l.getProductoId().equals(productoId))
                    .findFirst()
                    .orElse(null);
        }
        if (linea != null) {
            linea.setCantidadRecibida(linea.getCantidadRecibida().add(cantidadRecibida));
        }
    }

    private void validarYTransicionar(Transferencia transferencia,
                                       EstadoTransferenciaCodigo nuevoEstado) {
        EstadoTransferenciaCodigo estadoActual = EstadoTransferenciaCodigo.valueOf(
                transferencia.getEstadoTransferencia().getCodigo());

        if (!validadorEstado.esTransicionValida(estadoActual, nuevoEstado)) {
            throw new EstadoTransferenciaException(
                    estadoActual.getCodigo(), nuevoEstado.getCodigo());
        }

        EstadoTransferencia estado = buscarEstadoTransferencia(nuevoEstado);
        transferencia.setEstadoTransferencia(estado);
    }

    private Transferencia buscarTransferencia(Long empresaId, Long transferenciaId) {
        return transferenciaRepository.findById(transferenciaId)
                .filter(t -> t.getEmpresa().getId().equals(empresaId))
                .filter(t -> Boolean.TRUE.equals(t.getActivo()))
                .orElseThrow(() -> new EntidadNoEncontradaException(
                        "Transferencia", transferenciaId));
    }

    private Empresa buscarEmpresa(Long empresaId) {
        return empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EntidadNoEncontradaException("Empresa", empresaId));
    }

    private Bodega buscarBodega(Long empresaId, Long bodegaId) {
        return bodegaRepository.findById(bodegaId)
                .filter(b -> b.getEmpresa().getId().equals(empresaId))
                .filter(b -> Boolean.TRUE.equals(b.getActivo()))
                .orElseThrow(() -> new EntidadNoEncontradaException("Bodega", bodegaId));
    }

    private EstadoTransferencia buscarEstadoTransferencia(EstadoTransferenciaCodigo codigo) {
        return estadoTransferenciaRepository.findByCodigo(codigo.getCodigo())
                .orElseThrow(() -> new OperacionInvalidaException(
                        "Estado transferencia no encontrado: " + codigo.getCodigo()));
    }
}

package com.exodia.inventario.aplicacion.comando.impl;

import com.exodia.inventario.aplicacion.comando.MovimientoContenedorService;
import com.exodia.inventario.aplicacion.comando.OperacionService;
import com.exodia.inventario.aplicacion.consulta.StockQueryService;
import com.exodia.inventario.domain.enums.EstadoContenedorCodigo;
import com.exodia.inventario.domain.enums.TipoReferencia;
import com.exodia.inventario.domain.enums.TipoUbicacion;
import com.exodia.inventario.domain.enums.TipoOperacionCodigo;
import com.exodia.inventario.domain.evento.MovimientoContenedorRealizadoEvent;
import com.exodia.inventario.domain.modelo.catalogo.EstadoContenedor;
import com.exodia.inventario.domain.modelo.catalogo.Ubicacion;
import com.exodia.inventario.domain.modelo.contenedor.Contenedor;
import com.exodia.inventario.domain.modelo.contenedor.Operacion;
import com.exodia.inventario.excepcion.EntidadNoEncontradaException;
import com.exodia.inventario.excepcion.OperacionInvalidaException;
import com.exodia.inventario.interfaz.dto.peticion.MoverContenedorRequest;
import com.exodia.inventario.interfaz.dto.peticion.OperacionContenedorRequest;
import com.exodia.inventario.interfaz.dto.respuesta.MovimientoContenedorResponse;
import com.exodia.inventario.repositorio.catalogo.EstadoContenedorRepository;
import com.exodia.inventario.repositorio.catalogo.UbicacionRepository;
import com.exodia.inventario.repositorio.contenedor.ContenedorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovimientoContenedorServiceImpl implements MovimientoContenedorService {

    private final ContenedorRepository contenedorRepository;
    private final UbicacionRepository ubicacionRepository;
    private final EstadoContenedorRepository estadoContenedorRepository;
    private final StockQueryService stockQueryService;
    private final OperacionService operacionService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public MovimientoContenedorResponse mover(Long empresaId, Long contenedorId, MoverContenedorRequest request) {
        Contenedor contenedor = buscarContenedorConLock(empresaId, contenedorId);
        Ubicacion ubicacionDestino = buscarUbicacion(empresaId, request.ubicacionDestinoId());

        if (!contenedor.getBodega().getId().equals(ubicacionDestino.getBodega().getId())) {
            throw new OperacionInvalidaException(
                    "Los movimientos internos solo permiten cambio dentro de la misma bodega. Use transferencias.");
        }

        return moverContenedor(contenedor, ubicacionDestino, request.comentarios(), false);
    }

    @Override
    @Transactional
    public MovimientoContenedorResponse enviarAStandby(Long empresaId,
                                                       Long contenedorId,
                                                       OperacionContenedorRequest request) {
        Contenedor contenedor = buscarContenedorConLock(empresaId, contenedorId);
        Ubicacion standby = contenedor.getBodega().getUbicacionStandby();

        if (standby == null) {
            throw new OperacionInvalidaException(
                    "La bodega del contenedor no tiene ubicacionStandby configurada");
        }

        return moverContenedor(contenedor, standby, request.comentarios(), true);
    }

    @Override
    @Transactional
    public MovimientoContenedorResponse sacarDeStandby(Long empresaId,
                                                       Long contenedorId,
                                                       MoverContenedorRequest request) {
        Contenedor contenedor = buscarContenedorConLock(empresaId, contenedorId);
        Ubicacion ubicacionDestino = buscarUbicacion(empresaId, request.ubicacionDestinoId());

        if (!contenedor.getBodega().getId().equals(ubicacionDestino.getBodega().getId())) {
            throw new OperacionInvalidaException(
                    "Solo se puede sacar de standby a una ubicacion de la misma bodega");
        }
        if (!esStandby(contenedor.getUbicacion(), contenedor.getBodega().getUbicacionStandby())) {
            throw new OperacionInvalidaException("El contenedor no se encuentra actualmente en standby");
        }
        if (esStandby(ubicacionDestino, contenedor.getBodega().getUbicacionStandby())) {
            throw new OperacionInvalidaException("La ubicacion destino no puede ser una ubicacion standby");
        }

        return moverContenedor(contenedor, ubicacionDestino, request.comentarios(), false);
    }

    private MovimientoContenedorResponse moverContenedor(Contenedor contenedor,
                                                         Ubicacion ubicacionDestino,
                                                         String comentarios,
                                                         boolean destinoEsStandby) {
        Long bodegaOrigenId = contenedor.getBodega().getId();
        Long ubicacionOrigenId = contenedor.getUbicacion().getId();
        String estadoAnterior = contenedor.getEstado().getCodigo();

        validarMovimientoPermitido(contenedor, destinoEsStandby);

        if (ubicacionOrigenId.equals(ubicacionDestino.getId())) {
            throw new OperacionInvalidaException("La ubicacion destino no puede ser la misma ubicacion origen");
        }

        BigDecimal stockActual = stockQueryService.obtenerStockContenedor(contenedor.getId());
        if (stockActual.compareTo(BigDecimal.ZERO) <= 0) {
            throw new OperacionInvalidaException(
                    "No se puede mover un contenedor sin stock disponible en kardex");
        }

        BigDecimal cantidadReservada = stockQueryService.obtenerCantidadReservada(contenedor.getId());
        if (cantidadReservada.compareTo(BigDecimal.ZERO) > 0) {
            throw new OperacionInvalidaException(String.format(
                    "No se puede mover el contenedor %s porque tiene %s unidades reservadas activas",
                    contenedor.getCodigoBarras(), cantidadReservada.toPlainString()));
        }

        String comentarioBase = comentarios != null && !comentarios.isBlank()
                ? comentarios
                : String.format("Movimiento interno %s -> %s",
                contenedor.getUbicacion().getCodigo(), ubicacionDestino.getCodigo());

        Operacion salida = operacionService.crearOperacion(
                contenedor,
                TipoOperacionCodigo.SALIDA_MOVIMIENTO,
                stockActual,
                comentarioBase,
                TipoReferencia.MOVIMIENTO,
                contenedor.getId(),
                null);

        contenedor.setBodega(ubicacionDestino.getBodega());
        contenedor.setUbicacion(ubicacionDestino);
        contenedor.setEstado(resolverEstadoDestino(contenedor, destinoEsStandby));
        contenedorRepository.save(contenedor);

        Operacion entrada = operacionService.crearOperacion(
                contenedor,
                TipoOperacionCodigo.ENTRADA_MOVIMIENTO,
                stockActual,
                comentarioBase,
                TipoReferencia.MOVIMIENTO,
                contenedor.getId(),
                null);

        eventPublisher.publishEvent(new MovimientoContenedorRealizadoEvent(
                contenedor.getEmpresa().getId(),
                contenedor.getId(),
                contenedor.getProductoId(),
                bodegaOrigenId,
                ubicacionOrigenId,
                contenedor.getBodega().getId(),
                ubicacionDestino.getId(),
                stockActual));

        log.info("Contenedor {} movido de ubicacion {} a {}",
                contenedor.getCodigoBarras(), ubicacionOrigenId, ubicacionDestino.getId());

        return new MovimientoContenedorResponse(
                contenedor.getId(),
                contenedor.getCodigoBarras(),
                bodegaOrigenId,
                ubicacionOrigenId,
                contenedor.getBodega().getId(),
                ubicacionDestino.getId(),
                stockActual,
                estadoAnterior,
                contenedor.getEstado().getCodigo(),
                salida.getId(),
                entrada.getId());
    }

    private Contenedor buscarContenedorConLock(Long empresaId, Long contenedorId) {
        return contenedorRepository.findByIdForUpdate(contenedorId)
                .filter(c -> c.getEmpresa().getId().equals(empresaId))
                .filter(c -> Boolean.TRUE.equals(c.getActivo()))
                .orElseThrow(() -> new EntidadNoEncontradaException("Contenedor", contenedorId));
    }

    private Ubicacion buscarUbicacion(Long empresaId, Long ubicacionId) {
        return ubicacionRepository.findById(ubicacionId)
                .filter(u -> u.getBodega().getEmpresa().getId().equals(empresaId))
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .orElseThrow(() -> new EntidadNoEncontradaException("Ubicacion", ubicacionId));
    }

    private void validarMovimientoPermitido(Contenedor contenedor, boolean destinoEsStandby) {
        String estadoActual = contenedor.getEstado().getCodigo();

        if (!destinoEsStandby) {
            return;
        }

        if (!EstadoContenedorCodigo.DISPONIBLE.getCodigo().equals(estadoActual)) {
            throw new OperacionInvalidaException(String.format(
                    "Solo se pueden enviar a standby contenedores en estado DISPONIBLE. Estado actual: %s",
                    estadoActual));
        }
    }

    private EstadoContenedor resolverEstadoDestino(Contenedor contenedor, boolean destinoEsStandby) {
        if (destinoEsStandby) {
            return buscarEstado(EstadoContenedorCodigo.EN_STANDBY);
        }

        if (EstadoContenedorCodigo.EN_STANDBY.getCodigo().equals(contenedor.getEstado().getCodigo())) {
            return buscarEstado(EstadoContenedorCodigo.DISPONIBLE);
        }

        return contenedor.getEstado();
    }

    private EstadoContenedor buscarEstado(EstadoContenedorCodigo codigo) {
        return estadoContenedorRepository.findByCodigo(codigo.getCodigo())
                .orElseThrow(() -> new OperacionInvalidaException(
                        "Estado de contenedor no encontrado: " + codigo.getCodigo()));
    }

    private boolean esStandby(Ubicacion ubicacion, Ubicacion standbyConfigurada) {
        if (ubicacion == null) {
            return false;
        }
        if (standbyConfigurada != null && standbyConfigurada.getId().equals(ubicacion.getId())) {
            return true;
        }
        return ubicacion.getTipoUbicacion() == TipoUbicacion.STANDBY;
    }
}

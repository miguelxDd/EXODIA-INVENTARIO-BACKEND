package com.exodia.inventario.aplicacion.comando.impl;

import com.exodia.inventario.aplicacion.comando.UbicacionService;
import com.exodia.inventario.domain.enums.TipoUbicacion;
import com.exodia.inventario.domain.modelo.catalogo.Bodega;
import com.exodia.inventario.domain.modelo.catalogo.Ubicacion;
import com.exodia.inventario.excepcion.EntidadNoEncontradaException;
import com.exodia.inventario.interfaz.dto.peticion.ActualizarUbicacionRequest;
import com.exodia.inventario.interfaz.dto.peticion.CrearUbicacionRequest;
import com.exodia.inventario.interfaz.dto.respuesta.UbicacionResponse;
import com.exodia.inventario.interfaz.mapeador.UbicacionMapeador;
import com.exodia.inventario.repositorio.catalogo.BodegaRepository;
import com.exodia.inventario.repositorio.catalogo.UbicacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UbicacionServiceImpl implements UbicacionService {

    private final UbicacionRepository ubicacionRepository;
    private final BodegaRepository bodegaRepository;
    private final UbicacionMapeador ubicacionMapeador;

    @Override
    @Transactional
    public UbicacionResponse crear(Long empresaId, CrearUbicacionRequest request) {
        Bodega bodega = bodegaRepository.findById(request.bodegaId())
                .filter(b -> b.getEmpresa().getId().equals(empresaId))
                .filter(b -> Boolean.TRUE.equals(b.getActivo()))
                .orElseThrow(() -> new EntidadNoEncontradaException("Bodega", request.bodegaId()));

        TipoUbicacion tipo = request.tipoUbicacion() != null
                ? TipoUbicacion.valueOf(request.tipoUbicacion())
                : TipoUbicacion.GENERAL;

        Ubicacion ubicacion = Ubicacion.builder()
                .bodega(bodega)
                .codigo(request.codigo())
                .nombre(request.nombre())
                .codigoBarras(request.codigoBarras())
                .tipoUbicacion(tipo)
                .build();

        ubicacion = ubicacionRepository.save(ubicacion);
        log.info("Ubicacion {} creada en bodega {}", ubicacion.getCodigo(), request.bodegaId());
        return ubicacionMapeador.toResponse(ubicacion);
    }

    @Override
    @Transactional(readOnly = true)
    public UbicacionResponse obtenerPorId(Long empresaId, Long ubicacionId) {
        Ubicacion ubicacion = buscarUbicacion(empresaId, ubicacionId);
        return ubicacionMapeador.toResponse(ubicacion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UbicacionResponse> listarPorBodega(Long empresaId, Long bodegaId) {
        // Validar que la bodega pertenece a la empresa
        bodegaRepository.findById(bodegaId)
                .filter(b -> b.getEmpresa().getId().equals(empresaId))
                .filter(b -> Boolean.TRUE.equals(b.getActivo()))
                .orElseThrow(() -> new EntidadNoEncontradaException("Bodega", bodegaId));

        List<Ubicacion> ubicaciones = ubicacionRepository.findByBodegaIdAndActivoTrue(bodegaId);
        return ubicacionMapeador.toResponseList(ubicaciones);
    }

    @Override
    @Transactional
    public UbicacionResponse actualizar(Long empresaId, Long ubicacionId, ActualizarUbicacionRequest request) {
        Ubicacion ubicacion = buscarUbicacion(empresaId, ubicacionId);

        if (request.nombre() != null) ubicacion.setNombre(request.nombre());
        if (request.codigoBarras() != null) ubicacion.setCodigoBarras(request.codigoBarras());
        if (request.tipoUbicacion() != null) ubicacion.setTipoUbicacion(TipoUbicacion.valueOf(request.tipoUbicacion()));

        ubicacion = ubicacionRepository.save(ubicacion);
        log.info("Ubicacion {} actualizada", ubicacionId);
        return ubicacionMapeador.toResponse(ubicacion);
    }

    @Override
    @Transactional
    public void desactivar(Long empresaId, Long ubicacionId) {
        Ubicacion ubicacion = buscarUbicacion(empresaId, ubicacionId);
        ubicacion.setActivo(false);
        ubicacionRepository.save(ubicacion);
        log.info("Ubicacion {} desactivada", ubicacionId);
    }

    private Ubicacion buscarUbicacion(Long empresaId, Long ubicacionId) {
        return ubicacionRepository.findById(ubicacionId)
                .filter(u -> u.getBodega().getEmpresa().getId().equals(empresaId))
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .orElseThrow(() -> new EntidadNoEncontradaException("Ubicacion", ubicacionId));
    }
}

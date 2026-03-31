package com.exodia.inventario.aplicacion.comando.impl;

import com.exodia.inventario.aplicacion.comando.ConfigMermaService;
import com.exodia.inventario.domain.modelo.catalogo.Bodega;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;
import com.exodia.inventario.domain.modelo.extension.ConfigMerma;
import com.exodia.inventario.excepcion.EntidadNoEncontradaException;
import com.exodia.inventario.excepcion.OperacionInvalidaException;
import com.exodia.inventario.interfaz.dto.peticion.ActualizarConfigMermaRequest;
import com.exodia.inventario.interfaz.dto.peticion.CrearConfigMermaRequest;
import com.exodia.inventario.interfaz.dto.respuesta.ConfigMermaResponse;
import com.exodia.inventario.interfaz.mapeador.ConfigMermaMapeador;
import com.exodia.inventario.repositorio.catalogo.BodegaRepository;
import com.exodia.inventario.repositorio.catalogo.EmpresaRepository;
import com.exodia.inventario.repositorio.extension.ConfigMermaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfigMermaServiceImpl implements ConfigMermaService {

    private final ConfigMermaRepository configMermaRepository;
    private final EmpresaRepository empresaRepository;
    private final BodegaRepository bodegaRepository;
    private final ConfigMermaMapeador configMermaMapeador;

    @Override
    @Transactional
    public ConfigMermaResponse crear(Long empresaId, CrearConfigMermaRequest request) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EntidadNoEncontradaException("Empresa", empresaId));

        Bodega bodega = null;
        if (request.bodegaId() != null) {
            bodega = bodegaRepository.findById(request.bodegaId())
                    .filter(b -> b.getEmpresa().getId().equals(empresaId))
                    .filter(b -> Boolean.TRUE.equals(b.getActivo()))
                    .orElseThrow(() -> new EntidadNoEncontradaException("Bodega", request.bodegaId()));
        }

        // Validar unicidad empresa+producto+bodega
        configMermaRepository.findByEmpresaIdAndProductoIdAndBodegaIdAndActivoTrue(
                empresaId, request.productoId(), request.bodegaId())
                .ifPresent(existing -> {
                    throw new OperacionInvalidaException(
                            "Ya existe configuracion de merma para producto "
                                    + request.productoId() + " en bodega " + request.bodegaId());
                });

        ConfigMerma config = ConfigMerma.builder()
                .empresa(empresa)
                .productoId(request.productoId())
                .bodega(bodega)
                .tipoMerma(request.tipoMerma())
                .porcentajeMerma(request.porcentajeMerma())
                .cantidadFijaMerma(request.cantidadFijaMerma())
                .frecuenciaDias(request.frecuenciaDias())
                .build();

        config = configMermaRepository.save(config);

        log.info("ConfigMerma creada: producto={}, bodega={}, tipo={}",
                request.productoId(), request.bodegaId(), request.tipoMerma());

        return configMermaMapeador.toResponse(config);
    }

    @Override
    @Transactional(readOnly = true)
    public ConfigMermaResponse obtenerPorId(Long empresaId, Long id) {
        ConfigMerma config = configMermaRepository.findById(id)
                .filter(c -> c.getEmpresa().getId().equals(empresaId))
                .filter(c -> Boolean.TRUE.equals(c.getActivo()))
                .orElseThrow(() -> new EntidadNoEncontradaException("ConfigMerma", id));
        return configMermaMapeador.toResponse(config);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConfigMermaResponse> listarPorEmpresa(Long empresaId) {
        return configMermaMapeador.toResponseList(
                configMermaRepository.findByEmpresaIdAndActivoTrue(empresaId));
    }

    @Override
    @Transactional
    public ConfigMermaResponse actualizar(Long empresaId, Long id, ActualizarConfigMermaRequest request) {
        ConfigMerma config = configMermaRepository.findById(id)
                .filter(c -> c.getEmpresa().getId().equals(empresaId))
                .filter(c -> Boolean.TRUE.equals(c.getActivo()))
                .orElseThrow(() -> new EntidadNoEncontradaException("ConfigMerma", id));

        if (request.tipoMerma() != null) {
            config.setTipoMerma(request.tipoMerma());
        }
        if (request.porcentajeMerma() != null) {
            config.setPorcentajeMerma(request.porcentajeMerma());
        }
        if (request.cantidadFijaMerma() != null) {
            config.setCantidadFijaMerma(request.cantidadFijaMerma());
        }
        if (request.frecuenciaDias() != null) {
            config.setFrecuenciaDias(request.frecuenciaDias());
        }

        config = configMermaRepository.save(config);
        return configMermaMapeador.toResponse(config);
    }

    @Override
    @Transactional
    public void desactivar(Long empresaId, Long id) {
        ConfigMerma config = configMermaRepository.findById(id)
                .filter(c -> c.getEmpresa().getId().equals(empresaId))
                .filter(c -> Boolean.TRUE.equals(c.getActivo()))
                .orElseThrow(() -> new EntidadNoEncontradaException("ConfigMerma", id));

        config.setActivo(false);
        configMermaRepository.save(config);

        log.info("ConfigMerma {} desactivada", id);
    }
}

package com.exodia.inventario.aplicacion.comando.impl;

import com.exodia.inventario.aplicacion.comando.ConfiguracionProductoService;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;
import com.exodia.inventario.domain.modelo.catalogo.Unidad;
import com.exodia.inventario.domain.modelo.extension.ConfiguracionProducto;
import com.exodia.inventario.excepcion.EntidadNoEncontradaException;
import com.exodia.inventario.excepcion.OperacionInvalidaException;
import com.exodia.inventario.interfaz.dto.peticion.ActualizarConfiguracionProductoRequest;
import com.exodia.inventario.interfaz.dto.peticion.CrearConfiguracionProductoRequest;
import com.exodia.inventario.interfaz.dto.respuesta.ConfiguracionProductoResponse;
import com.exodia.inventario.interfaz.mapeador.ConfiguracionProductoMapeador;
import com.exodia.inventario.repositorio.catalogo.EmpresaRepository;
import com.exodia.inventario.repositorio.catalogo.UnidadRepository;
import com.exodia.inventario.repositorio.extension.ConfiguracionProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfiguracionProductoServiceImpl implements ConfiguracionProductoService {

    private final ConfiguracionProductoRepository configuracionProductoRepository;
    private final EmpresaRepository empresaRepository;
    private final UnidadRepository unidadRepository;
    private final ConfiguracionProductoMapeador configuracionProductoMapeador;

    @Override
    @Transactional
    public ConfiguracionProductoResponse obtenerOCrear(Long empresaId, Long productoId) {
        ConfiguracionProducto config = configuracionProductoRepository
                .findByEmpresaIdAndProductoId(empresaId, productoId)
                .orElseGet(() -> crearConfigDefault(empresaId, productoId));
        return configuracionProductoMapeador.toResponse(config);
    }

    @Override
    @Transactional
    public ConfiguracionProductoResponse crear(Long empresaId, CrearConfiguracionProductoRequest request) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EntidadNoEncontradaException("Empresa", empresaId));

        // Validar unicidad
        configuracionProductoRepository.findByEmpresaIdAndProductoId(empresaId, request.productoId())
                .ifPresent(existing -> {
                    throw new OperacionInvalidaException(
                            "Ya existe configuracion para producto " + request.productoId());
                });

        Unidad unidadBase = null;
        if (request.unidadBaseId() != null) {
            unidadBase = unidadRepository.findById(request.unidadBaseId())
                    .filter(u -> u.getEmpresa().getId().equals(empresaId))
                    .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                    .orElseThrow(() -> new EntidadNoEncontradaException("Unidad", request.unidadBaseId()));
        }

        ConfiguracionProducto config = ConfiguracionProducto.builder()
                .empresa(empresa)
                .productoId(request.productoId())
                .manejaLote(request.manejaLote() != null ? request.manejaLote() : false)
                .manejaVencimiento(request.manejaVencimiento() != null ? request.manejaVencimiento() : false)
                .toleranciaMerma(request.toleranciaMerma() != null ? request.toleranciaMerma() : java.math.BigDecimal.ZERO)
                .unidadBase(unidadBase)
                .build();

        config = configuracionProductoRepository.save(config);

        log.info("ConfiguracionProducto creada: empresa={}, producto={}", empresaId, request.productoId());

        return configuracionProductoMapeador.toResponse(config);
    }

    @Override
    @Transactional
    public ConfiguracionProductoResponse actualizar(Long empresaId, Long productoId,
                                                      ActualizarConfiguracionProductoRequest request) {
        ConfiguracionProducto config = configuracionProductoRepository
                .findByEmpresaIdAndProductoId(empresaId, productoId)
                .filter(c -> Boolean.TRUE.equals(c.getActivo()))
                .orElseThrow(() -> new EntidadNoEncontradaException(
                        "ConfiguracionProducto", productoId));

        if (request.manejaLote() != null) {
            config.setManejaLote(request.manejaLote());
        }
        if (request.manejaVencimiento() != null) {
            config.setManejaVencimiento(request.manejaVencimiento());
        }
        if (request.toleranciaMerma() != null) {
            config.setToleranciaMerma(request.toleranciaMerma());
        }
        if (request.unidadBaseId() != null) {
            Unidad unidadBase = unidadRepository.findById(request.unidadBaseId())
                    .filter(u -> u.getEmpresa().getId().equals(empresaId))
                    .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                    .orElseThrow(() -> new EntidadNoEncontradaException("Unidad", request.unidadBaseId()));
            config.setUnidadBase(unidadBase);
        }

        config = configuracionProductoRepository.save(config);
        return configuracionProductoMapeador.toResponse(config);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConfiguracionProductoResponse> listarPorEmpresa(Long empresaId) {
        return configuracionProductoMapeador.toResponseList(
                configuracionProductoRepository.findByEmpresaIdAndActivoTrue(empresaId));
    }

    private ConfiguracionProducto crearConfigDefault(Long empresaId, Long productoId) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EntidadNoEncontradaException("Empresa", empresaId));

        ConfiguracionProducto config = ConfiguracionProducto.builder()
                .empresa(empresa)
                .productoId(productoId)
                .build();

        config = configuracionProductoRepository.save(config);
        log.info("ConfiguracionProducto creada con defaults: empresa={}, producto={}", empresaId, productoId);

        return config;
    }
}

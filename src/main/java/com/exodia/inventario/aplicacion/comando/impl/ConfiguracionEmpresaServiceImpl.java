package com.exodia.inventario.aplicacion.comando.impl;

import com.exodia.inventario.aplicacion.comando.ConfiguracionEmpresaService;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;
import com.exodia.inventario.domain.modelo.extension.ConfiguracionEmpresa;
import com.exodia.inventario.excepcion.EntidadNoEncontradaException;
import com.exodia.inventario.excepcion.OperacionInvalidaException;
import com.exodia.inventario.interfaz.dto.peticion.ActualizarConfiguracionEmpresaRequest;
import com.exodia.inventario.interfaz.dto.respuesta.ConfiguracionEmpresaResponse;
import com.exodia.inventario.interfaz.mapeador.ConfiguracionEmpresaMapeador;
import com.exodia.inventario.repositorio.catalogo.EmpresaRepository;
import com.exodia.inventario.repositorio.extension.ConfiguracionEmpresaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfiguracionEmpresaServiceImpl implements ConfiguracionEmpresaService {

    private static final Set<String> POLITICAS_VALIDAS = Set.of("FEFO", "FIFO", "MANUAL");

    private final ConfiguracionEmpresaRepository configuracionEmpresaRepository;
    private final EmpresaRepository empresaRepository;
    private final ConfiguracionEmpresaMapeador configuracionEmpresaMapeador;

    @Override
    @Transactional
    public ConfiguracionEmpresaResponse obtenerOCrear(Long empresaId) {
        ConfiguracionEmpresa config = obtenerEntidadOCrear(empresaId);
        return configuracionEmpresaMapeador.toResponse(config);
    }

    @Override
    @Transactional
    public ConfiguracionEmpresaResponse actualizar(Long empresaId,
                                                     ActualizarConfiguracionEmpresaRequest request) {
        ConfiguracionEmpresa config = obtenerEntidadOCrear(empresaId);

        if (request.expiracionReservaHoras() != null) {
            config.setExpiracionReservaHoras(request.expiracionReservaHoras());
        }
        if (request.diasAlertaVencimiento() != null) {
            config.setDiasAlertaVencimiento(request.diasAlertaVencimiento());
        }
        if (request.barcodePrefijo() != null) {
            config.setBarcodePrefijo(request.barcodePrefijo());
        }
        if (request.barcodeLongitudPadding() != null) {
            config.setBarcodeLongitudPadding(request.barcodeLongitudPadding());
        }
        if (request.politicaSalida() != null) {
            if (!POLITICAS_VALIDAS.contains(request.politicaSalida())) {
                throw new OperacionInvalidaException(
                        "Politica de salida invalida: " + request.politicaSalida()
                                + ". Valores validos: " + POLITICAS_VALIDAS);
            }
            config.setPoliticaSalida(request.politicaSalida());
        }

        config = configuracionEmpresaRepository.save(config);
        log.info("ConfiguracionEmpresa actualizada para empresa {}", empresaId);

        return configuracionEmpresaMapeador.toResponse(config);
    }

    @Override
    @Transactional
    public ConfiguracionEmpresa obtenerEntidadOCrear(Long empresaId) {
        return configuracionEmpresaRepository.findByEmpresaId(empresaId)
                .orElseGet(() -> crearConfigDefault(empresaId));
    }

    private ConfiguracionEmpresa crearConfigDefault(Long empresaId) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EntidadNoEncontradaException("Empresa", empresaId));

        ConfiguracionEmpresa config = ConfiguracionEmpresa.builder()
                .empresa(empresa)
                .build();

        config = configuracionEmpresaRepository.save(config);
        log.info("ConfiguracionEmpresa creada con defaults para empresa {}", empresaId);

        return config;
    }
}

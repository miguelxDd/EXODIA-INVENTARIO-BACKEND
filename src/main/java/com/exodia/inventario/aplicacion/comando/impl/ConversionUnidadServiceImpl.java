package com.exodia.inventario.aplicacion.comando.impl;

import com.exodia.inventario.aplicacion.comando.ConversionUnidadService;
import com.exodia.inventario.domain.enums.TipoOperacionConversion;
import com.exodia.inventario.domain.modelo.catalogo.ConversionUnidad;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;
import com.exodia.inventario.domain.modelo.catalogo.Unidad;
import com.exodia.inventario.excepcion.EntidadNoEncontradaException;
import com.exodia.inventario.interfaz.dto.peticion.ActualizarConversionUnidadRequest;
import com.exodia.inventario.interfaz.dto.peticion.CrearConversionUnidadRequest;
import com.exodia.inventario.interfaz.dto.respuesta.ConversionUnidadResponse;
import com.exodia.inventario.interfaz.mapeador.ConversionUnidadMapeador;
import com.exodia.inventario.repositorio.catalogo.ConversionUnidadRepository;
import com.exodia.inventario.repositorio.catalogo.EmpresaRepository;
import com.exodia.inventario.repositorio.catalogo.UnidadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversionUnidadServiceImpl implements ConversionUnidadService {

    private final ConversionUnidadRepository conversionUnidadRepository;
    private final UnidadRepository unidadRepository;
    private final EmpresaRepository empresaRepository;
    private final ConversionUnidadMapeador conversionUnidadMapeador;

    @Override
    @Transactional
    public ConversionUnidadResponse crear(Long empresaId, CrearConversionUnidadRequest request) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EntidadNoEncontradaException("Empresa", empresaId));

        Unidad unidadOrigen = unidadRepository.findById(request.unidadOrigenId())
                .filter(u -> u.getEmpresa().getId().equals(empresaId))
                .orElseThrow(() -> new EntidadNoEncontradaException("Unidad origen", request.unidadOrigenId()));

        Unidad unidadDestino = unidadRepository.findById(request.unidadDestinoId())
                .filter(u -> u.getEmpresa().getId().equals(empresaId))
                .orElseThrow(() -> new EntidadNoEncontradaException("Unidad destino", request.unidadDestinoId()));

        ConversionUnidad conversion = ConversionUnidad.builder()
                .empresa(empresa)
                .unidadOrigen(unidadOrigen)
                .unidadDestino(unidadDestino)
                .factorConversion(request.factorConversion())
                .tipoOperacion(TipoOperacionConversion.valueOf(request.tipoOperacion()))
                .productoId(request.productoId())
                .build();

        conversion = conversionUnidadRepository.save(conversion);
        log.info("Conversion de unidad creada: {} -> {} para empresa {}",
                unidadOrigen.getCodigo(), unidadDestino.getCodigo(), empresaId);
        return conversionUnidadMapeador.toResponse(conversion);
    }

    @Override
    @Transactional(readOnly = true)
    public ConversionUnidadResponse obtenerPorId(Long empresaId, Long conversionId) {
        ConversionUnidad conversion = buscarConversion(empresaId, conversionId);
        return conversionUnidadMapeador.toResponse(conversion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversionUnidadResponse> listarPorEmpresa(Long empresaId) {
        List<ConversionUnidad> conversiones = conversionUnidadRepository.findByEmpresaIdAndActivoTrue(empresaId);
        return conversionUnidadMapeador.toResponseList(conversiones);
    }

    @Override
    @Transactional
    public ConversionUnidadResponse actualizar(Long empresaId, Long conversionId,
                                                ActualizarConversionUnidadRequest request) {
        ConversionUnidad conversion = buscarConversion(empresaId, conversionId);

        if (request.factorConversion() != null) {
            conversion.setFactorConversion(request.factorConversion());
        }
        if (request.tipoOperacion() != null) {
            conversion.setTipoOperacion(TipoOperacionConversion.valueOf(request.tipoOperacion()));
        }

        conversion = conversionUnidadRepository.save(conversion);
        log.info("Conversion de unidad {} actualizada", conversionId);
        return conversionUnidadMapeador.toResponse(conversion);
    }

    @Override
    @Transactional
    public void desactivar(Long empresaId, Long conversionId) {
        ConversionUnidad conversion = buscarConversion(empresaId, conversionId);
        conversion.setActivo(false);
        conversionUnidadRepository.save(conversion);
        log.info("Conversion de unidad {} desactivada", conversionId);
    }

    private ConversionUnidad buscarConversion(Long empresaId, Long conversionId) {
        return conversionUnidadRepository.findById(conversionId)
                .filter(c -> c.getEmpresa().getId().equals(empresaId))
                .filter(c -> Boolean.TRUE.equals(c.getActivo()))
                .orElseThrow(() -> new EntidadNoEncontradaException("ConversionUnidad", conversionId));
    }
}

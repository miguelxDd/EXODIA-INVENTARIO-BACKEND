package com.exodia.inventario.aplicacion.comando.impl;

import com.exodia.inventario.aplicacion.comando.MaximoMinimoService;
import com.exodia.inventario.domain.modelo.catalogo.Bodega;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;
import com.exodia.inventario.domain.modelo.catalogo.Unidad;
import com.exodia.inventario.domain.modelo.extension.MaximoMinimo;
import com.exodia.inventario.excepcion.EntidadNoEncontradaException;
import com.exodia.inventario.excepcion.OperacionInvalidaException;
import com.exodia.inventario.interfaz.dto.peticion.ActualizarMaximoMinimoRequest;
import com.exodia.inventario.interfaz.dto.peticion.CrearMaximoMinimoRequest;
import com.exodia.inventario.interfaz.dto.respuesta.MaximoMinimoResponse;
import com.exodia.inventario.interfaz.mapeador.MaximoMinimoMapeador;
import com.exodia.inventario.repositorio.catalogo.BodegaRepository;
import com.exodia.inventario.repositorio.catalogo.EmpresaRepository;
import com.exodia.inventario.repositorio.catalogo.UnidadRepository;
import com.exodia.inventario.repositorio.extension.MaximoMinimoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MaximoMinimoServiceImpl implements MaximoMinimoService {

    private final MaximoMinimoRepository maximoMinimoRepository;
    private final EmpresaRepository empresaRepository;
    private final BodegaRepository bodegaRepository;
    private final UnidadRepository unidadRepository;
    private final MaximoMinimoMapeador maximoMinimoMapeador;

    @Override
    @Transactional
    public MaximoMinimoResponse crear(Long empresaId, CrearMaximoMinimoRequest request) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EntidadNoEncontradaException("Empresa", empresaId));

        Bodega bodega = bodegaRepository.findById(request.bodegaId())
                .filter(b -> b.getEmpresa().getId().equals(empresaId))
                .filter(b -> Boolean.TRUE.equals(b.getActivo()))
                .orElseThrow(() -> new EntidadNoEncontradaException("Bodega", request.bodegaId()));

        Unidad unidad = unidadRepository.findById(request.unidadId())
                .filter(u -> u.getEmpresa().getId().equals(empresaId))
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .orElseThrow(() -> new EntidadNoEncontradaException("Unidad", request.unidadId()));

        // Validar que no exista ya para esa combinacion
        maximoMinimoRepository.findByEmpresaIdAndProductoIdAndBodegaId(
                empresaId, request.productoId(), request.bodegaId())
                .ifPresent(existing -> {
                    throw new OperacionInvalidaException(
                            "Ya existe configuracion de max/min para producto "
                                    + request.productoId() + " en bodega " + request.bodegaId());
                });

        if (request.stockMinimo().compareTo(request.stockMaximo()) > 0) {
            throw new OperacionInvalidaException(
                    "El stock minimo no puede ser mayor que el stock maximo");
        }

        MaximoMinimo maxMin = MaximoMinimo.builder()
                .empresa(empresa)
                .productoId(request.productoId())
                .bodega(bodega)
                .unidad(unidad)
                .stockMinimo(request.stockMinimo())
                .stockMaximo(request.stockMaximo())
                .puntoReorden(request.puntoReorden())
                .build();

        maxMin = maximoMinimoRepository.save(maxMin);

        log.info("MaximoMinimo creado: producto={}, bodega={}, min={}, max={}",
                request.productoId(), bodega.getCodigo(),
                request.stockMinimo(), request.stockMaximo());

        return maximoMinimoMapeador.toResponse(maxMin);
    }

    @Override
    @Transactional(readOnly = true)
    public MaximoMinimoResponse obtenerPorId(Long empresaId, Long id) {
        MaximoMinimo maxMin = maximoMinimoRepository.findById(id)
                .filter(m -> m.getEmpresa().getId().equals(empresaId))
                .filter(m -> Boolean.TRUE.equals(m.getActivo()))
                .orElseThrow(() -> new EntidadNoEncontradaException("MaximoMinimo", id));
        return maximoMinimoMapeador.toResponse(maxMin);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaximoMinimoResponse> listarPorBodega(Long empresaId, Long bodegaId) {
        return maximoMinimoMapeador.toResponseList(
                maximoMinimoRepository.findByEmpresaIdAndBodegaId(empresaId, bodegaId));
    }

    @Override
    @Transactional
    public MaximoMinimoResponse actualizar(Long empresaId, Long id, ActualizarMaximoMinimoRequest request) {
        MaximoMinimo maxMin = maximoMinimoRepository.findById(id)
                .filter(m -> m.getEmpresa().getId().equals(empresaId))
                .filter(m -> Boolean.TRUE.equals(m.getActivo()))
                .orElseThrow(() -> new EntidadNoEncontradaException("MaximoMinimo", id));

        if (request.stockMinimo() != null) {
            maxMin.setStockMinimo(request.stockMinimo());
        }
        if (request.stockMaximo() != null) {
            maxMin.setStockMaximo(request.stockMaximo());
        }
        if (request.puntoReorden() != null) {
            maxMin.setPuntoReorden(request.puntoReorden());
        }

        if (maxMin.getStockMinimo().compareTo(maxMin.getStockMaximo()) > 0) {
            throw new OperacionInvalidaException(
                    "El stock minimo no puede ser mayor que el stock maximo");
        }

        maxMin = maximoMinimoRepository.save(maxMin);
        return maximoMinimoMapeador.toResponse(maxMin);
    }

    @Override
    @Transactional
    public void desactivar(Long empresaId, Long id) {
        MaximoMinimo maxMin = maximoMinimoRepository.findById(id)
                .filter(m -> m.getEmpresa().getId().equals(empresaId))
                .filter(m -> Boolean.TRUE.equals(m.getActivo()))
                .orElseThrow(() -> new EntidadNoEncontradaException("MaximoMinimo", id));

        maxMin.setActivo(false);
        maximoMinimoRepository.save(maxMin);

        log.info("MaximoMinimo {} desactivado", id);
    }
}

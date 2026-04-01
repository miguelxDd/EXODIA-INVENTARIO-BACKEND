package com.exodia.inventario.aplicacion.comando.impl;

import com.exodia.inventario.aplicacion.comando.BodegaService;
import com.exodia.inventario.domain.modelo.catalogo.Bodega;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;
import com.exodia.inventario.excepcion.EntidadNoEncontradaException;
import com.exodia.inventario.interfaz.dto.peticion.ActualizarBodegaRequest;
import com.exodia.inventario.interfaz.dto.peticion.CrearBodegaRequest;
import com.exodia.inventario.interfaz.dto.respuesta.BodegaResponse;
import com.exodia.inventario.interfaz.mapeador.BodegaMapeador;
import com.exodia.inventario.repositorio.catalogo.BodegaRepository;
import com.exodia.inventario.repositorio.catalogo.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BodegaServiceImpl implements BodegaService {

    private final BodegaRepository bodegaRepository;
    private final EmpresaRepository empresaRepository;
    private final BodegaMapeador bodegaMapeador;

    @Override
    @Transactional
    public BodegaResponse crear(Long empresaId, CrearBodegaRequest request) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EntidadNoEncontradaException("Empresa", empresaId));

        Bodega bodega = Bodega.builder()
                .empresa(empresa)
                .codigo(request.codigo())
                .nombre(request.nombre())
                .direccion(request.direccion())
                .ciudad(request.ciudad())
                .pais(request.pais())
                .esProductoTerminado(request.esProductoTerminado() != null ? request.esProductoTerminado() : false)
                .esConsignacion(request.esConsignacion() != null ? request.esConsignacion() : false)
                .build();

        bodega = bodegaRepository.save(bodega);
        log.info("Bodega {} creada para empresa {}", bodega.getCodigo(), empresaId);
        return bodegaMapeador.toResponse(bodega);
    }

    @Override
    @Transactional(readOnly = true)
    public BodegaResponse obtenerPorId(Long empresaId, Long bodegaId) {
        Bodega bodega = buscarBodega(empresaId, bodegaId);
        return bodegaMapeador.toResponse(bodega);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BodegaResponse> listarPorEmpresa(Long empresaId) {
        List<Bodega> bodegas = bodegaRepository.findByEmpresaIdAndActivoTrue(empresaId);
        return bodegaMapeador.toResponseList(bodegas);
    }

    @Override
    @Transactional
    public BodegaResponse actualizar(Long empresaId, Long bodegaId, ActualizarBodegaRequest request) {
        Bodega bodega = buscarBodega(empresaId, bodegaId);

        if (request.nombre() != null) bodega.setNombre(request.nombre());
        if (request.direccion() != null) bodega.setDireccion(request.direccion());
        if (request.ciudad() != null) bodega.setCiudad(request.ciudad());
        if (request.pais() != null) bodega.setPais(request.pais());
        if (request.esProductoTerminado() != null) bodega.setEsProductoTerminado(request.esProductoTerminado());
        if (request.esConsignacion() != null) bodega.setEsConsignacion(request.esConsignacion());

        bodega = bodegaRepository.save(bodega);
        log.info("Bodega {} actualizada", bodegaId);
        return bodegaMapeador.toResponse(bodega);
    }

    @Override
    @Transactional
    public void desactivar(Long empresaId, Long bodegaId) {
        Bodega bodega = buscarBodega(empresaId, bodegaId);
        bodega.setActivo(false);
        bodegaRepository.save(bodega);
        log.info("Bodega {} desactivada", bodegaId);
    }

    private Bodega buscarBodega(Long empresaId, Long bodegaId) {
        return bodegaRepository.findById(bodegaId)
                .filter(b -> b.getEmpresa().getId().equals(empresaId))
                .filter(b -> Boolean.TRUE.equals(b.getActivo()))
                .orElseThrow(() -> new EntidadNoEncontradaException("Bodega", bodegaId));
    }
}

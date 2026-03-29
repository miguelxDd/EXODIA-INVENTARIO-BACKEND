package com.exodia.inventario.aplicacion.comando.impl;

import com.exodia.inventario.aplicacion.comando.UnidadService;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;
import com.exodia.inventario.domain.modelo.catalogo.Unidad;
import com.exodia.inventario.excepcion.EntidadNoEncontradaException;
import com.exodia.inventario.interfaz.dto.peticion.ActualizarUnidadRequest;
import com.exodia.inventario.interfaz.dto.peticion.CrearUnidadRequest;
import com.exodia.inventario.interfaz.dto.respuesta.UnidadResponse;
import com.exodia.inventario.interfaz.mapeador.UnidadMapeador;
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
public class UnidadServiceImpl implements UnidadService {

    private final UnidadRepository unidadRepository;
    private final EmpresaRepository empresaRepository;
    private final UnidadMapeador unidadMapeador;

    @Override
    @Transactional
    public UnidadResponse crear(Long empresaId, CrearUnidadRequest request) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EntidadNoEncontradaException("Empresa", empresaId));

        Unidad unidad = Unidad.builder()
                .empresa(empresa)
                .codigo(request.codigo())
                .nombre(request.nombre())
                .abreviatura(request.abreviatura())
                .build();

        unidad = unidadRepository.save(unidad);
        log.info("Unidad {} creada para empresa {}", unidad.getCodigo(), empresaId);
        return unidadMapeador.toResponse(unidad);
    }

    @Override
    @Transactional(readOnly = true)
    public UnidadResponse obtenerPorId(Long empresaId, Long unidadId) {
        Unidad unidad = buscarUnidad(empresaId, unidadId);
        return unidadMapeador.toResponse(unidad);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnidadResponse> listarPorEmpresa(Long empresaId) {
        List<Unidad> unidades = unidadRepository.findByEmpresaIdAndActivoTrue(empresaId);
        return unidadMapeador.toResponseList(unidades);
    }

    @Override
    @Transactional
    public UnidadResponse actualizar(Long empresaId, Long unidadId, ActualizarUnidadRequest request) {
        Unidad unidad = buscarUnidad(empresaId, unidadId);

        if (request.nombre() != null) unidad.setNombre(request.nombre());
        if (request.abreviatura() != null) unidad.setAbreviatura(request.abreviatura());

        unidad = unidadRepository.save(unidad);
        log.info("Unidad {} actualizada", unidadId);
        return unidadMapeador.toResponse(unidad);
    }

    @Override
    @Transactional
    public void desactivar(Long empresaId, Long unidadId) {
        Unidad unidad = buscarUnidad(empresaId, unidadId);
        unidad.setActivo(false);
        unidadRepository.save(unidad);
        log.info("Unidad {} desactivada", unidadId);
    }

    private Unidad buscarUnidad(Long empresaId, Long unidadId) {
        return unidadRepository.findById(unidadId)
                .filter(u -> u.getEmpresa().getId().equals(empresaId))
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .orElseThrow(() -> new EntidadNoEncontradaException("Unidad", unidadId));
    }
}

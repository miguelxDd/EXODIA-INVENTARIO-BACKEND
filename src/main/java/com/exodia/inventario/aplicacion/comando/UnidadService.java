package com.exodia.inventario.aplicacion.comando;

import com.exodia.inventario.interfaz.dto.peticion.ActualizarUnidadRequest;
import com.exodia.inventario.interfaz.dto.peticion.CrearUnidadRequest;
import com.exodia.inventario.interfaz.dto.respuesta.UnidadResponse;

import java.util.List;

/**
 * Servicio CRUD para unidades de medida.
 */
public interface UnidadService {

    UnidadResponse crear(Long empresaId, CrearUnidadRequest request);

    UnidadResponse obtenerPorId(Long empresaId, Long unidadId);

    List<UnidadResponse> listarPorEmpresa(Long empresaId);

    UnidadResponse actualizar(Long empresaId, Long unidadId, ActualizarUnidadRequest request);

    void desactivar(Long empresaId, Long unidadId);
}

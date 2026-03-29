package com.exodia.inventario.aplicacion.comando;

import com.exodia.inventario.interfaz.dto.peticion.ActualizarBodegaRequest;
import com.exodia.inventario.interfaz.dto.peticion.CrearBodegaRequest;
import com.exodia.inventario.interfaz.dto.respuesta.BodegaResponse;

import java.util.List;

/**
 * Servicio CRUD para bodegas.
 */
public interface BodegaService {

    BodegaResponse crear(Long empresaId, CrearBodegaRequest request);

    BodegaResponse obtenerPorId(Long empresaId, Long bodegaId);

    List<BodegaResponse> listarPorEmpresa(Long empresaId);

    BodegaResponse actualizar(Long empresaId, Long bodegaId, ActualizarBodegaRequest request);

    void desactivar(Long empresaId, Long bodegaId);
}

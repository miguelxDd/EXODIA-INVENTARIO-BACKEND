package com.exodia.inventario.aplicacion.comando;

import com.exodia.inventario.interfaz.dto.peticion.ActualizarUbicacionRequest;
import com.exodia.inventario.interfaz.dto.peticion.CrearUbicacionRequest;
import com.exodia.inventario.interfaz.dto.respuesta.UbicacionResponse;

import java.util.List;

/**
 * Servicio CRUD para ubicaciones.
 */
public interface UbicacionService {

    UbicacionResponse crear(Long empresaId, CrearUbicacionRequest request);

    UbicacionResponse obtenerPorId(Long empresaId, Long ubicacionId);

    List<UbicacionResponse> listarPorBodega(Long empresaId, Long bodegaId);

    UbicacionResponse actualizar(Long empresaId, Long ubicacionId, ActualizarUbicacionRequest request);

    void desactivar(Long empresaId, Long ubicacionId);
}

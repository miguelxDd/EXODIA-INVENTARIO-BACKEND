package com.exodia.inventario.aplicacion.comando;

import com.exodia.inventario.interfaz.dto.peticion.CrearMaximoMinimoRequest;
import com.exodia.inventario.interfaz.dto.peticion.ActualizarMaximoMinimoRequest;
import com.exodia.inventario.interfaz.dto.respuesta.MaximoMinimoResponse;

import java.util.List;

public interface MaximoMinimoService {

    MaximoMinimoResponse crear(Long empresaId, CrearMaximoMinimoRequest request);

    MaximoMinimoResponse obtenerPorId(Long empresaId, Long id);

    List<MaximoMinimoResponse> listarPorBodega(Long empresaId, Long bodegaId);

    MaximoMinimoResponse actualizar(Long empresaId, Long id, ActualizarMaximoMinimoRequest request);

    void desactivar(Long empresaId, Long id);
}

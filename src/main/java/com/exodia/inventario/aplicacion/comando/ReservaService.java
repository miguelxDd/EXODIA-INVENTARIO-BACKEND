package com.exodia.inventario.aplicacion.comando;

import com.exodia.inventario.interfaz.dto.peticion.CrearReservaRequest;
import com.exodia.inventario.interfaz.dto.respuesta.ReservaResponse;

import java.util.List;

public interface ReservaService {

    ReservaResponse crear(Long empresaId, CrearReservaRequest request);

    ReservaResponse obtenerPorId(Long empresaId, Long reservaId);

    List<ReservaResponse> listarPorContenedor(Long empresaId, Long contenedorId);

    ReservaResponse cancelar(Long empresaId, Long reservaId);
}

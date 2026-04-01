package com.exodia.inventario.aplicacion.comando;

import com.exodia.inventario.interfaz.dto.peticion.CrearOrdenPickingRequest;
import com.exodia.inventario.interfaz.dto.respuesta.OrdenPickingResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PickingService {

    OrdenPickingResponse crear(Long empresaId, CrearOrdenPickingRequest request);

    OrdenPickingResponse ejecutar(Long empresaId, Long ordenId);

    OrdenPickingResponse obtenerPorId(Long empresaId, Long ordenId);

    Page<OrdenPickingResponse> listarPorEmpresa(Long empresaId, Pageable pageable);

    OrdenPickingResponse cancelar(Long empresaId, Long ordenId);
}

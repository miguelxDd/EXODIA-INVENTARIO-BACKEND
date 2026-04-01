package com.exodia.inventario.aplicacion.comando;

import com.exodia.inventario.interfaz.dto.peticion.CrearConteoFisicoRequest;
import com.exodia.inventario.interfaz.dto.peticion.RegistrarConteoLineaRequest;
import com.exodia.inventario.interfaz.dto.respuesta.ConteoFisicoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ConteoFisicoService {

    ConteoFisicoResponse crear(Long empresaId, CrearConteoFisicoRequest request);

    ConteoFisicoResponse registrarLinea(Long empresaId, Long conteoId, RegistrarConteoLineaRequest request);

    ConteoFisicoResponse aplicar(Long empresaId, Long conteoId);

    ConteoFisicoResponse obtenerPorId(Long empresaId, Long conteoId);

    Page<ConteoFisicoResponse> listarPorEmpresa(Long empresaId, Pageable pageable);

    ConteoFisicoResponse cancelar(Long empresaId, Long conteoId);
}

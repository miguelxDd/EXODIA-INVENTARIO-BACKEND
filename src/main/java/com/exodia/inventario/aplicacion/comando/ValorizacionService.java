package com.exodia.inventario.aplicacion.comando;

import com.exodia.inventario.interfaz.dto.respuesta.FotoCostoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ValorizacionService {

    void generarFotoCosto(Long empresaId);

    Page<FotoCostoResponse> listarFotosCosto(Long empresaId, Pageable pageable);
}

package com.exodia.inventario.aplicacion.comando;

import com.exodia.inventario.interfaz.dto.peticion.MoverContenedorRequest;
import com.exodia.inventario.interfaz.dto.peticion.OperacionContenedorRequest;
import com.exodia.inventario.interfaz.dto.respuesta.MovimientoContenedorResponse;

public interface MovimientoContenedorService {

    MovimientoContenedorResponse mover(Long empresaId, Long contenedorId, MoverContenedorRequest request);

    MovimientoContenedorResponse enviarAStandby(Long empresaId,
                                                Long contenedorId,
                                                OperacionContenedorRequest request);

    MovimientoContenedorResponse sacarDeStandby(Long empresaId,
                                                Long contenedorId,
                                                MoverContenedorRequest request);
}

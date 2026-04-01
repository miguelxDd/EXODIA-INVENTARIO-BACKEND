package com.exodia.inventario.aplicacion.consulta;

import com.exodia.inventario.interfaz.dto.respuesta.EtiquetaResponse;

public interface EtiquetaInventarioService {

    EtiquetaResponse generarEtiquetaContenedor(Long empresaId, Long contenedorId);

    EtiquetaResponse generarEtiquetaUbicacion(Long empresaId, Long ubicacionId);
}

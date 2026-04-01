package com.exodia.inventario.aplicacion.comando;

import com.exodia.inventario.interfaz.dto.peticion.ConvertirInventarioRequest;
import com.exodia.inventario.interfaz.dto.respuesta.ConversionInventarioResponse;

public interface ConversionInventarioService {

    ConversionInventarioResponse convertir(Long empresaId, ConvertirInventarioRequest request);
}

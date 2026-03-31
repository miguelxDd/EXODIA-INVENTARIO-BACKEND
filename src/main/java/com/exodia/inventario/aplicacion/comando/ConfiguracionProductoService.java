package com.exodia.inventario.aplicacion.comando;

import com.exodia.inventario.interfaz.dto.peticion.ActualizarConfiguracionProductoRequest;
import com.exodia.inventario.interfaz.dto.peticion.CrearConfiguracionProductoRequest;
import com.exodia.inventario.interfaz.dto.respuesta.ConfiguracionProductoResponse;

import java.util.List;

public interface ConfiguracionProductoService {

    ConfiguracionProductoResponse obtenerOCrear(Long empresaId, Long productoId);

    ConfiguracionProductoResponse crear(Long empresaId, CrearConfiguracionProductoRequest request);

    ConfiguracionProductoResponse actualizar(Long empresaId, Long productoId,
                                               ActualizarConfiguracionProductoRequest request);

    List<ConfiguracionProductoResponse> listarPorEmpresa(Long empresaId);
}

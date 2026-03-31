package com.exodia.inventario.aplicacion.comando;

import com.exodia.inventario.interfaz.dto.peticion.ActualizarConfigMermaRequest;
import com.exodia.inventario.interfaz.dto.peticion.CrearConfigMermaRequest;
import com.exodia.inventario.interfaz.dto.respuesta.ConfigMermaResponse;

import java.util.List;

public interface ConfigMermaService {

    ConfigMermaResponse crear(Long empresaId, CrearConfigMermaRequest request);

    ConfigMermaResponse obtenerPorId(Long empresaId, Long id);

    List<ConfigMermaResponse> listarPorEmpresa(Long empresaId);

    ConfigMermaResponse actualizar(Long empresaId, Long id, ActualizarConfigMermaRequest request);

    void desactivar(Long empresaId, Long id);
}

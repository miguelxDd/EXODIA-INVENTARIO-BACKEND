package com.exodia.inventario.aplicacion.comando;

import com.exodia.inventario.domain.modelo.extension.ConfiguracionEmpresa;
import com.exodia.inventario.interfaz.dto.peticion.ActualizarConfiguracionEmpresaRequest;
import com.exodia.inventario.interfaz.dto.respuesta.ConfiguracionEmpresaResponse;

public interface ConfiguracionEmpresaService {

    ConfiguracionEmpresaResponse obtenerOCrear(Long empresaId);

    ConfiguracionEmpresaResponse actualizar(Long empresaId, ActualizarConfiguracionEmpresaRequest request);

    ConfiguracionEmpresa obtenerEntidadOCrear(Long empresaId);
}

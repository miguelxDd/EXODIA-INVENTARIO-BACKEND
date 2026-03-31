package com.exodia.inventario.interfaz.mapeador;

import com.exodia.inventario.domain.modelo.extension.ConfiguracionEmpresa;
import com.exodia.inventario.interfaz.dto.respuesta.ConfiguracionEmpresaResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConfiguracionEmpresaMapeador {

    @Mapping(source = "empresa.id", target = "empresaId")
    ConfiguracionEmpresaResponse toResponse(ConfiguracionEmpresa config);
}

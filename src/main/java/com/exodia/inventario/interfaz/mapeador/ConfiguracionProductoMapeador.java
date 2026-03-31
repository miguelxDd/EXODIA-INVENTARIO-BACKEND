package com.exodia.inventario.interfaz.mapeador;

import com.exodia.inventario.domain.modelo.extension.ConfiguracionProducto;
import com.exodia.inventario.interfaz.dto.respuesta.ConfiguracionProductoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ConfiguracionProductoMapeador {

    @Mapping(source = "unidadBase.id", target = "unidadBaseId")
    ConfiguracionProductoResponse toResponse(ConfiguracionProducto config);

    List<ConfiguracionProductoResponse> toResponseList(List<ConfiguracionProducto> lista);
}

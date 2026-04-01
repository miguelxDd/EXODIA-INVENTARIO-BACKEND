package com.exodia.inventario.interfaz.mapeador;

import com.exodia.inventario.domain.modelo.extension.ConfigMerma;
import com.exodia.inventario.interfaz.dto.respuesta.ConfigMermaResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ConfigMermaMapeador {

    @Mapping(source = "bodega.id", target = "bodegaId")
    ConfigMermaResponse toResponse(ConfigMerma configMerma);

    List<ConfigMermaResponse> toResponseList(List<ConfigMerma> lista);
}

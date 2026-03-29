package com.exodia.inventario.interfaz.mapeador;

import com.exodia.inventario.domain.modelo.extension.MaximoMinimo;
import com.exodia.inventario.interfaz.dto.respuesta.MaximoMinimoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MaximoMinimoMapeador {

    @Mapping(source = "bodega.id", target = "bodegaId")
    @Mapping(source = "unidad.id", target = "unidadId")
    MaximoMinimoResponse toResponse(MaximoMinimo maximoMinimo);

    List<MaximoMinimoResponse> toResponseList(List<MaximoMinimo> lista);
}

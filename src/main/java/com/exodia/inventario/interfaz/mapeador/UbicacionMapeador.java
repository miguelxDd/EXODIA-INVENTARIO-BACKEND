package com.exodia.inventario.interfaz.mapeador;

import com.exodia.inventario.domain.modelo.catalogo.Ubicacion;
import com.exodia.inventario.interfaz.dto.respuesta.UbicacionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UbicacionMapeador {

    @Mapping(source = "bodega.id", target = "bodegaId")
    @Mapping(source = "tipoUbicacion", target = "tipoUbicacion")
    UbicacionResponse toResponse(Ubicacion ubicacion);

    List<UbicacionResponse> toResponseList(List<Ubicacion> ubicaciones);
}

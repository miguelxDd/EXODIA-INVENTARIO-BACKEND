package com.exodia.inventario.interfaz.mapeador;

import com.exodia.inventario.domain.modelo.extension.FotoCosto;
import com.exodia.inventario.interfaz.dto.respuesta.FotoCostoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FotoCostoMapeador {

    @Mapping(source = "bodega.id", target = "bodegaId")
    @Mapping(source = "unidad.id", target = "unidadId")
    FotoCostoResponse toResponse(FotoCosto fotoCosto);
}

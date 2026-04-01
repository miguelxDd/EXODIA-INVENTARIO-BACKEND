package com.exodia.inventario.interfaz.mapeador;

import com.exodia.inventario.domain.modelo.catalogo.Bodega;
import com.exodia.inventario.interfaz.dto.respuesta.BodegaResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BodegaMapeador {

    @Mapping(source = "ubicacionStandby.id", target = "ubicacionStandbyId")
    BodegaResponse toResponse(Bodega bodega);

    List<BodegaResponse> toResponseList(List<Bodega> bodegas);
}

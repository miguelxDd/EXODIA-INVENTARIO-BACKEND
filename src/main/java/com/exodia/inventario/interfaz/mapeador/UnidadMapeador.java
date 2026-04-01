package com.exodia.inventario.interfaz.mapeador;

import com.exodia.inventario.domain.modelo.catalogo.Unidad;
import com.exodia.inventario.interfaz.dto.respuesta.UnidadResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UnidadMapeador {

    UnidadResponse toResponse(Unidad unidad);

    List<UnidadResponse> toResponseList(List<Unidad> unidades);
}

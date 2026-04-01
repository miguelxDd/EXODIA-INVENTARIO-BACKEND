package com.exodia.inventario.interfaz.mapeador;

import com.exodia.inventario.domain.modelo.recepcion.Recepcion;
import com.exodia.inventario.domain.modelo.recepcion.RecepcionLinea;
import com.exodia.inventario.interfaz.dto.respuesta.RecepcionLineaResponse;
import com.exodia.inventario.interfaz.dto.respuesta.RecepcionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RecepcionMapeador {

    @Mapping(source = "bodega.id", target = "bodegaId")
    @Mapping(source = "tipoRecepcion", target = "tipoRecepcion")
    RecepcionResponse toResponse(Recepcion recepcion);

    @Mapping(source = "contenedor.id", target = "contenedorId")
    @Mapping(source = "contenedor.codigoBarras", target = "codigoBarras")
    @Mapping(source = "unidad.id", target = "unidadId")
    @Mapping(source = "ubicacion.id", target = "ubicacionId")
    RecepcionLineaResponse toLineaResponse(RecepcionLinea linea);

    List<RecepcionLineaResponse> toLineaResponseList(List<RecepcionLinea> lineas);
}

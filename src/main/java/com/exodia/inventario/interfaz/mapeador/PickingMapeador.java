package com.exodia.inventario.interfaz.mapeador;

import com.exodia.inventario.domain.modelo.picking.OrdenPicking;
import com.exodia.inventario.domain.modelo.picking.PickingLinea;
import com.exodia.inventario.interfaz.dto.respuesta.OrdenPickingResponse;
import com.exodia.inventario.interfaz.dto.respuesta.PickingLineaResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PickingMapeador {

    @Mapping(source = "bodega.id", target = "bodegaId")
    OrdenPickingResponse toResponse(OrdenPicking orden);

    @Mapping(source = "unidad.id", target = "unidadId")
    @Mapping(source = "contenedor.id", target = "contenedorId")
    @Mapping(source = "operacion.id", target = "operacionId")
    PickingLineaResponse toLineaResponse(PickingLinea linea);

    List<PickingLineaResponse> toLineaResponseList(List<PickingLinea> lineas);
}

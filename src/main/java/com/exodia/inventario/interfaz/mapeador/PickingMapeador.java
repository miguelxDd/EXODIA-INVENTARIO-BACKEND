package com.exodia.inventario.interfaz.mapeador;

import com.exodia.inventario.domain.modelo.picking.OrdenPicking;
import com.exodia.inventario.domain.modelo.picking.PickingLineaAsignacion;
import com.exodia.inventario.domain.modelo.picking.PickingLinea;
import com.exodia.inventario.interfaz.dto.respuesta.PickingLineaAsignacionResponse;
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
    @Mapping(source = "contenedorId", target = "contenedorSolicitadoId")
    @Mapping(source = "contenedor.id", target = "contenedorId")
    @Mapping(source = "operacion.id", target = "operacionId")
    @Mapping(source = "asignaciones", target = "asignaciones")
    PickingLineaResponse toLineaResponse(PickingLinea linea);

    List<PickingLineaResponse> toLineaResponseList(List<PickingLinea> lineas);

    @Mapping(source = "contenedor.id", target = "contenedorId")
    @Mapping(source = "contenedor.codigoBarras", target = "codigoBarras")
    @Mapping(source = "operacion.id", target = "operacionId")
    PickingLineaAsignacionResponse toAsignacionResponse(PickingLineaAsignacion asignacion);

    List<PickingLineaAsignacionResponse> toAsignacionResponseList(List<PickingLineaAsignacion> asignaciones);
}

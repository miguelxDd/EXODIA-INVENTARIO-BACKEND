package com.exodia.inventario.interfaz.mapeador;

import com.exodia.inventario.domain.modelo.conteo.ConteoFisico;
import com.exodia.inventario.domain.modelo.conteo.ConteoLinea;
import com.exodia.inventario.interfaz.dto.respuesta.ConteoFisicoResponse;
import com.exodia.inventario.interfaz.dto.respuesta.ConteoLineaResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ConteoFisicoMapeador {

    @Mapping(source = "bodega.id", target = "bodegaId")
    @Mapping(source = "ajusteGenerado.id", target = "ajusteGeneradoId")
    ConteoFisicoResponse toResponse(ConteoFisico conteo);

    @Mapping(source = "contenedor.id", target = "contenedorId")
    ConteoLineaResponse toLineaResponse(ConteoLinea linea);

    List<ConteoLineaResponse> toLineaResponseList(List<ConteoLinea> lineas);
}

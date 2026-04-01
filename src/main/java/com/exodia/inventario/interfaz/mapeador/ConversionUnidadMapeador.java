package com.exodia.inventario.interfaz.mapeador;

import com.exodia.inventario.domain.modelo.catalogo.ConversionUnidad;
import com.exodia.inventario.interfaz.dto.respuesta.ConversionUnidadResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ConversionUnidadMapeador {

    @Mapping(source = "unidadOrigen.id", target = "unidadOrigenId")
    @Mapping(source = "unidadOrigen.codigo", target = "unidadOrigenCodigo")
    @Mapping(source = "unidadDestino.id", target = "unidadDestinoId")
    @Mapping(source = "unidadDestino.codigo", target = "unidadDestinoCodigo")
    @Mapping(source = "tipoOperacion", target = "tipoOperacion")
    ConversionUnidadResponse toResponse(ConversionUnidad conversion);

    List<ConversionUnidadResponse> toResponseList(List<ConversionUnidad> conversiones);
}

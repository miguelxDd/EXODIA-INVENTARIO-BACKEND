package com.exodia.inventario.interfaz.mapeador;

import com.exodia.inventario.interfaz.dto.respuesta.ContenedorStockResponse;
import com.exodia.inventario.interfaz.dto.respuesta.ProductoBodegaStockResponse;
import com.exodia.inventario.repositorio.proyeccion.ContenedorStockProjection;
import com.exodia.inventario.repositorio.proyeccion.ProductoBodegaStockProjection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StockMapeador {

    @Mapping(source = "cantidadDisponible", target = "cantidadDisponible")
    ContenedorStockResponse toResponse(ContenedorStockProjection projection);

    List<ContenedorStockResponse> toResponseList(List<ContenedorStockProjection> projections);

    ProductoBodegaStockResponse toResponse(ProductoBodegaStockProjection projection);

    List<ProductoBodegaStockResponse> toProductoBodegaResponseList(
            List<ProductoBodegaStockProjection> projections);
}

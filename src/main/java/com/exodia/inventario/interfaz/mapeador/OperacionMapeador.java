package com.exodia.inventario.interfaz.mapeador;

import com.exodia.inventario.domain.modelo.contenedor.Operacion;
import com.exodia.inventario.interfaz.dto.respuesta.OperacionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OperacionMapeador {

    @Mapping(source = "contenedor.id", target = "contenedorId")
    @Mapping(source = "bodega.id", target = "bodegaId")
    @Mapping(source = "ubicacion.id", target = "ubicacionId")
    @Mapping(source = "unidad.id", target = "unidadId")
    @Mapping(source = "tipoOperacion.codigo", target = "tipoOperacionCodigo")
    OperacionResponse toResponse(Operacion operacion);

    List<OperacionResponse> toResponseList(List<Operacion> operaciones);
}

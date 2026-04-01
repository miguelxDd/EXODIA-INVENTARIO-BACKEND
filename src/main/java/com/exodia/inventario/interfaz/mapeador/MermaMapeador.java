package com.exodia.inventario.interfaz.mapeador;

import com.exodia.inventario.domain.modelo.extension.RegistroMerma;
import com.exodia.inventario.interfaz.dto.respuesta.MermaResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MermaMapeador {

    @Mapping(source = "contenedor.id", target = "contenedorId")
    @Mapping(source = "motivoCodigo", target = "motivoCodigo")
    @Mapping(source = "operacion.id", target = "operacionId")
    MermaResponse toResponse(RegistroMerma registro);
}

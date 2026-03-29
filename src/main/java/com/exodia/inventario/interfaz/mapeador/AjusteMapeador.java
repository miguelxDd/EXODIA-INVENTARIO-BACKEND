package com.exodia.inventario.interfaz.mapeador;

import com.exodia.inventario.domain.modelo.ajuste.Ajuste;
import com.exodia.inventario.domain.modelo.ajuste.AjusteLinea;
import com.exodia.inventario.interfaz.dto.respuesta.AjusteLineaResponse;
import com.exodia.inventario.interfaz.dto.respuesta.AjusteResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AjusteMapeador {

    @Mapping(source = "bodega.id", target = "bodegaId")
    @Mapping(source = "tipoAjuste.codigo", target = "tipoAjusteCodigo")
    @Mapping(source = "tipoAjuste.nombre", target = "tipoAjusteNombre")
    AjusteResponse toResponse(Ajuste ajuste);

    @Mapping(source = "contenedor.id", target = "contenedorId")
    @Mapping(source = "contenedor.codigoBarras", target = "codigoBarras")
    AjusteLineaResponse toLineaResponse(AjusteLinea linea);

    List<AjusteLineaResponse> toLineaResponseList(List<AjusteLinea> lineas);
}

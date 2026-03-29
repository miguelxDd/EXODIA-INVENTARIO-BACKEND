package com.exodia.inventario.interfaz.mapeador;

import com.exodia.inventario.domain.modelo.transferencia.Transferencia;
import com.exodia.inventario.domain.modelo.transferencia.TransferenciaContenedor;
import com.exodia.inventario.domain.modelo.transferencia.TransferenciaLinea;
import com.exodia.inventario.interfaz.dto.respuesta.TransferenciaContenedorResponse;
import com.exodia.inventario.interfaz.dto.respuesta.TransferenciaLineaResponse;
import com.exodia.inventario.interfaz.dto.respuesta.TransferenciaResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransferenciaMapeador {

    @Mapping(source = "tipoTransferencia", target = "tipoTransferencia")
    @Mapping(source = "bodegaOrigen.id", target = "bodegaOrigenId")
    @Mapping(source = "bodegaOrigen.codigo", target = "bodegaOrigenCodigo")
    @Mapping(source = "bodegaDestino.id", target = "bodegaDestinoId")
    @Mapping(source = "bodegaDestino.codigo", target = "bodegaDestinoCodigo")
    @Mapping(source = "estadoTransferencia.codigo", target = "estadoCodigo")
    TransferenciaResponse toResponse(Transferencia transferencia);

    @Mapping(source = "unidad.id", target = "unidadId")
    TransferenciaLineaResponse toLineaResponse(TransferenciaLinea linea);

    List<TransferenciaLineaResponse> toLineaResponseList(List<TransferenciaLinea> lineas);

    @Mapping(source = "contenedor.id", target = "contenedorId")
    @Mapping(source = "contenedor.codigoBarras", target = "codigoBarras")
    TransferenciaContenedorResponse toContenedorResponse(TransferenciaContenedor contenedor);

    List<TransferenciaContenedorResponse> toContenedorResponseList(
            List<TransferenciaContenedor> contenedores);
}

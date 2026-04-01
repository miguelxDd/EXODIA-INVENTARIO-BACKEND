package com.exodia.inventario.interfaz.mapeador;

import com.exodia.inventario.domain.modelo.contenedor.Reserva;
import com.exodia.inventario.interfaz.dto.respuesta.ReservaResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReservaMapeador {

    @Mapping(source = "contenedor.id", target = "contenedorId")
    @Mapping(source = "bodega.id", target = "bodegaId")
    ReservaResponse toResponse(Reserva reserva);

    List<ReservaResponse> toResponseList(List<Reserva> reservas);
}

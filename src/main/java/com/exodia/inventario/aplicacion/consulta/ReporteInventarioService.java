package com.exodia.inventario.aplicacion.consulta;

import com.exodia.inventario.interfaz.dto.respuesta.AuxiliarInventarioResponse;
import com.exodia.inventario.interfaz.dto.respuesta.ValorizacionActualResponse;

import java.time.OffsetDateTime;
import java.util.List;

public interface ReporteInventarioService {

    AuxiliarInventarioResponse generarAuxiliarInventario(Long empresaId,
                                                         Long productoId,
                                                         Long bodegaId,
                                                         OffsetDateTime fechaDesde,
                                                         OffsetDateTime fechaHasta);

    String exportarAuxiliarInventarioCsv(Long empresaId,
                                         Long productoId,
                                         Long bodegaId,
                                         OffsetDateTime fechaDesde,
                                         OffsetDateTime fechaHasta);

    List<ValorizacionActualResponse> obtenerValorizacionActual(Long empresaId,
                                                               Long bodegaId,
                                                               Long productoId);

    String exportarValorizacionActualCsv(Long empresaId,
                                         Long bodegaId,
                                         Long productoId);
}

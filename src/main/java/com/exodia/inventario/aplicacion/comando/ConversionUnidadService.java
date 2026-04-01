package com.exodia.inventario.aplicacion.comando;

import com.exodia.inventario.interfaz.dto.peticion.ActualizarConversionUnidadRequest;
import com.exodia.inventario.interfaz.dto.peticion.CrearConversionUnidadRequest;
import com.exodia.inventario.interfaz.dto.respuesta.ConversionUnidadResponse;

import java.util.List;

/**
 * Servicio CRUD para conversiones de unidad.
 */
public interface ConversionUnidadService {

    ConversionUnidadResponse crear(Long empresaId, CrearConversionUnidadRequest request);

    ConversionUnidadResponse obtenerPorId(Long empresaId, Long conversionId);

    List<ConversionUnidadResponse> listarPorEmpresa(Long empresaId);

    ConversionUnidadResponse actualizar(Long empresaId, Long conversionId, ActualizarConversionUnidadRequest request);

    void desactivar(Long empresaId, Long conversionId);
}

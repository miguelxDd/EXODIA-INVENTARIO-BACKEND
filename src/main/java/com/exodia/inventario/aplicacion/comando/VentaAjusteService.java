package com.exodia.inventario.aplicacion.comando;

import com.exodia.inventario.interfaz.dto.peticion.CrearAjusteVentaRequest;
import com.exodia.inventario.interfaz.dto.respuesta.AjusteResponse;

/**
 * Ajusta inventario contra una venta/factura ya confirmada en el dominio comercial.
 */
public interface VentaAjusteService {

    AjusteResponse crear(Long empresaId, CrearAjusteVentaRequest request);
}

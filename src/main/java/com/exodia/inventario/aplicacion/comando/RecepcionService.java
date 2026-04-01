package com.exodia.inventario.aplicacion.comando;

import com.exodia.inventario.interfaz.dto.peticion.CrearRecepcionRequest;
import com.exodia.inventario.interfaz.dto.respuesta.RecepcionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Servicio de recepcion de inventario.
 * Orquesta: crea contenedores, genera barcodes, registra operaciones
 * en el kardex y publica evento de inventario recibido.
 */
public interface RecepcionService {

    /**
     * Crea una recepcion completa con todas sus lineas.
     * Por cada linea: crea contenedor, genera barcode, crea operacion RECEPCION.
     *
     * @param empresaId ID de la empresa
     * @param request   datos de la recepcion
     * @return recepcion creada con sus lineas
     */
    RecepcionResponse crear(Long empresaId, CrearRecepcionRequest request);

    /**
     * Obtiene una recepcion por su ID.
     *
     * @param empresaId   ID de la empresa
     * @param recepcionId ID de la recepcion
     * @return recepcion con sus lineas
     */
    RecepcionResponse obtenerPorId(Long empresaId, Long recepcionId);

    /**
     * Lista recepciones por empresa, paginado.
     *
     * @param empresaId ID de la empresa
     * @param pageable  paginacion
     * @return pagina de recepciones
     */
    Page<RecepcionResponse> listarPorEmpresa(Long empresaId, Pageable pageable);
}

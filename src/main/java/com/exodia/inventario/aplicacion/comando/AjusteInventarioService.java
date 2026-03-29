package com.exodia.inventario.aplicacion.comando;

import com.exodia.inventario.interfaz.dto.peticion.CrearAjusteRequest;
import com.exodia.inventario.interfaz.dto.respuesta.AjusteResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Servicio de ajustes de inventario.
 * Orquesta ajustes de cantidad y/o precio sobre contenedores.
 * Para ajustes negativos usa lock pesimista y valida stock disponible.
 */
public interface AjusteInventarioService {

    /**
     * Crea un ajuste de inventario con todas sus lineas.
     * Por cada linea: valida stock (si negativo), crea operacion, registra ajuste.
     *
     * @param empresaId ID de la empresa
     * @param request   datos del ajuste
     * @return ajuste creado con sus lineas
     */
    AjusteResponse crear(Long empresaId, CrearAjusteRequest request);

    /**
     * Obtiene un ajuste por su ID.
     *
     * @param empresaId ID de la empresa
     * @param ajusteId  ID del ajuste
     * @return ajuste con sus lineas
     */
    AjusteResponse obtenerPorId(Long empresaId, Long ajusteId);

    /**
     * Lista ajustes por empresa, paginado.
     *
     * @param empresaId ID de la empresa
     * @param pageable  paginacion
     * @return pagina de ajustes
     */
    Page<AjusteResponse> listarPorEmpresa(Long empresaId, Pageable pageable);
}

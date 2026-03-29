package com.exodia.inventario.aplicacion.comando;

import com.exodia.inventario.interfaz.dto.peticion.CrearTransferenciaRequest;
import com.exodia.inventario.interfaz.dto.peticion.RecibirTransferenciaRequest;
import com.exodia.inventario.interfaz.dto.respuesta.TransferenciaResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Servicio de transferencias de inventario entre bodegas.
 * Orquesta la maquina de estados:
 * BORRADOR -> CONFIRMADO -> DESPACHADO -> EN_TRANSITO -> RECIBIDO_PARCIAL/RECIBIDO_COMPLETO
 *
 * Soporta dos tipos:
 * - POR_CONTENEDOR: el usuario indica contenedores especificos.
 * - POR_PRODUCTO: el sistema resuelve contenedores automaticamente via FEFO.
 *
 * Usa lock pesimista ordenado por ID para despacho (deduccion de stock).
 */
public interface TransferenciaService {

    /**
     * Crea una transferencia en estado BORRADOR.
     */
    TransferenciaResponse crear(Long empresaId, CrearTransferenciaRequest request);

    /**
     * Obtiene una transferencia por ID.
     */
    TransferenciaResponse obtenerPorId(Long empresaId, Long transferenciaId);

    /**
     * Lista transferencias por empresa, paginado.
     */
    Page<TransferenciaResponse> listarPorEmpresa(Long empresaId, Pageable pageable);

    /**
     * BORRADOR -> CONFIRMADO. Valida que las lineas tengan datos completos.
     */
    TransferenciaResponse confirmar(Long empresaId, Long transferenciaId);

    /**
     * CONFIRMADO -> DESPACHADO -> EN_TRANSITO.
     * Resuelve contenedores (FEFO si POR_PRODUCTO), adquiere locks ordenados,
     * crea operaciones SALIDA_TRANSFERENCIA, cambia estado contenedores a EN_TRANSITO.
     * Publica TransferenciaDespachadaEvent.
     */
    TransferenciaResponse despachar(Long empresaId, Long transferenciaId);

    /**
     * EN_TRANSITO -> RECIBIDO_PARCIAL o RECIBIDO_COMPLETO.
     * Crea operaciones ENTRADA_TRANSFERENCIA en bodega destino.
     * Publica TransferenciaRecibidaEvent.
     */
    TransferenciaResponse recibir(Long empresaId, Long transferenciaId,
                                   RecibirTransferenciaRequest request);

    /**
     * BORRADOR/CONFIRMADO -> CANCELADO.
     */
    TransferenciaResponse cancelar(Long empresaId, Long transferenciaId);
}

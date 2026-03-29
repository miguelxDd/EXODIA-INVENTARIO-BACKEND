package com.exodia.inventario.aplicacion.comando;

/**
 * Servicio para generacion y validacion de codigos de barras.
 * Usa secuencia atomica por empresa+prefijo con lock pesimista.
 */
public interface BarcodeService {

    /**
     * Genera un nuevo codigo de barras unico para la empresa usando el prefijo por defecto.
     *
     * @param empresaId ID de la empresa
     * @return codigo de barras generado (ej: "INV00000001")
     */
    String generarBarcode(Long empresaId);

    /**
     * Genera un nuevo codigo de barras unico con prefijo especifico.
     *
     * @param empresaId ID de la empresa
     * @param prefijo   prefijo del barcode (ej: "INV", "REC")
     * @return codigo de barras generado (ej: "REC00000001")
     */
    String generarBarcode(Long empresaId, String prefijo);

    /**
     * Verifica si un codigo de barras ya existe para la empresa.
     *
     * @param empresaId    ID de la empresa
     * @param codigoBarras codigo a verificar
     * @return true si ya existe
     */
    boolean existeBarcode(Long empresaId, String codigoBarras);
}

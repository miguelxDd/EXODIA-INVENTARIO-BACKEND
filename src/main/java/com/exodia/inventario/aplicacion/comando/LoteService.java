package com.exodia.inventario.aplicacion.comando;

import com.exodia.inventario.domain.modelo.contenedor.Lote;

import java.time.LocalDate;

/**
 * Servicio para gestion de lotes.
 * Busca o crea lotes por empresa + numero + producto.
 */
public interface LoteService {

    /**
     * Busca un lote existente o crea uno nuevo.
     *
     * @param empresaId       ID de la empresa
     * @param productoId      ID del producto
     * @param numeroLote      numero de lote
     * @param fechaVencimiento fecha de vencimiento (opcional)
     * @param proveedorId     ID del proveedor (opcional)
     * @return lote encontrado o creado
     */
    Lote buscarOCrear(Long empresaId, Long productoId, String numeroLote,
                      LocalDate fechaVencimiento, Long proveedorId);
}

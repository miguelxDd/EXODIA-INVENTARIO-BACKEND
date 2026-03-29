package com.exodia.inventario.aplicacion.comando;

import com.exodia.inventario.domain.enums.TipoOperacionCodigo;
import com.exodia.inventario.domain.enums.TipoReferencia;
import com.exodia.inventario.domain.modelo.contenedor.Contenedor;
import com.exodia.inventario.domain.modelo.contenedor.Operacion;

import java.math.BigDecimal;

/**
 * Servicio central para crear operaciones en el kardex.
 * TODA creacion de operacion de inventario DEBE pasar por aqui.
 * Esto garantiza consistencia, validacion de stock y auditoria.
 */
public interface OperacionService {

    /**
     * Crea una operacion en el kardex con referencia cruzada.
     *
     * @param contenedor     contenedor sobre el que opera
     * @param tipoCodigo     tipo de operacion (define signo: +1, -1, 0)
     * @param cantidad       cantidad base (siempre positiva)
     * @param comentarios    comentarios opcionales
     * @param tipoReferencia tipo de documento origen (RECEPCION, TRANSFERENCIA, etc.)
     * @param referenciaId   ID del documento origen
     * @param referenciaLineaId ID de la linea del documento origen
     * @return la operacion creada y persistida
     */
    Operacion crearOperacion(Contenedor contenedor,
                             TipoOperacionCodigo tipoCodigo,
                             BigDecimal cantidad,
                             String comentarios,
                             TipoReferencia tipoReferencia,
                             Long referenciaId,
                             Long referenciaLineaId);

    /**
     * Crea una operacion en el kardex sin referencia cruzada.
     *
     * @param contenedor  contenedor sobre el que opera
     * @param tipoCodigo  tipo de operacion (define signo: +1, -1, 0)
     * @param cantidad    cantidad base (siempre positiva)
     * @param comentarios comentarios opcionales
     * @return la operacion creada y persistida
     */
    Operacion crearOperacion(Contenedor contenedor,
                             TipoOperacionCodigo tipoCodigo,
                             BigDecimal cantidad,
                             String comentarios);
}

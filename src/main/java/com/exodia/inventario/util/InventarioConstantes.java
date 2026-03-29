package com.exodia.inventario.util;

/**
 * Constantes globales del microservicio de inventario.
 */
public final class InventarioConstantes {

    private InventarioConstantes() {
        // No instanciable
    }

    // ── Paginacion ────────────────────────────────────────────────────────────
    public static final int MAX_RESULTADOS_PAGINA = 100;
    public static final int TAMANIO_PAGINA_DEFAULT = 20;

    // ── Barcode ───────────────────────────────────────────────────────────────
    public static final String BARCODE_PREFIJO_DEFAULT = "INV";
    public static final int BARCODE_LONGITUD_SECUENCIA = 8;

    // ── Escalas BigDecimal ────────────────────────────────────────────────────
    public static final int ESCALA_CANTIDAD = 6;
    public static final int ESCALA_PRECIO = 6;
    public static final int PRECISION_MONETARIA = 18;

    // ── Reservas ──────────────────────────────────────────────────────────────
    public static final int HORAS_EXPIRACION_RESERVA_DEFAULT = 48;

    // ── Vencimiento ───────────────────────────────────────────────────────────
    public static final int DIAS_ALERTA_VENCIMIENTO = 90;

    // ── Estados de reserva ────────────────────────────────────────────────────
    public static final String ESTADO_RESERVA_PENDIENTE = "PENDIENTE";
    public static final String ESTADO_RESERVA_PARCIAL = "PARCIAL";
    public static final String ESTADO_RESERVA_CUMPLIDA = "CUMPLIDA";
    public static final String ESTADO_RESERVA_CANCELADA = "CANCELADA";
    public static final String ESTADO_RESERVA_EXPIRADA = "EXPIRADA";

    // ── Recepcion ─────────────────────────────────────────────────────────────
    public static final String ESTADO_RECEPCION_CONFIRMADO = "CONFIRMADO";

    // ── Batch processing ──────────────────────────────────────────────────────
    public static final int BATCH_SIZE = 50;
}

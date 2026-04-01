package com.exodia.inventario.domain.modelo.contenedor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import com.exodia.inventario.domain.base.EntidadBase;
import com.exodia.inventario.domain.enums.TipoReferencia;
import com.exodia.inventario.domain.modelo.catalogo.Bodega;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;
import com.exodia.inventario.domain.modelo.catalogo.TipoAjuste;
import com.exodia.inventario.domain.modelo.catalogo.TipoOperacion;
import com.exodia.inventario.domain.modelo.catalogo.Ubicacion;
import com.exodia.inventario.domain.modelo.catalogo.Unidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "inv_operaciones")
public class Operacion extends EntidadBase {

    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contenedor_id", nullable = false)
    private Contenedor contenedor;

    @Column(name = "codigo_barras", nullable = false, length = 100)
    private String codigoBarras;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bodega_id", nullable = false)
    private Bodega bodega;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ubicacion_id", nullable = false)
    private Ubicacion ubicacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_id", nullable = false)
    private Unidad unidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_operacion_id", nullable = false)
    private TipoOperacion tipoOperacion;

    @Column(name = "cantidad", nullable = false, precision = 18, scale = 6)
    private BigDecimal cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 18, scale = 6)
    @Builder.Default
    private BigDecimal precioUnitario = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_ajuste_id")
    private TipoAjuste tipoAjuste;

    @Column(name = "numero_lote", length = 100)
    private String numeroLote;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "proveedor_id")
    private Long proveedorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_referencia", length = 50)
    private TipoReferencia tipoReferencia;

    @Column(name = "referencia_id")
    private Long referenciaId;

    @Column(name = "referencia_linea_id")
    private Long referenciaLineaId;

    @Column(name = "comentarios", columnDefinition = "TEXT")
    private String comentarios;

    @Column(name = "fecha_operacion", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaOperacion = OffsetDateTime.now();

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "creado_por")
    private Long creadoPor;

    @Column(name = "creado_en", updatable = false, nullable = false)
    private OffsetDateTime creadoEn;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime ahora = OffsetDateTime.now();
        if (this.creadoEn == null) {
            this.creadoEn = ahora;
        }
        if (this.fechaOperacion == null) {
            this.fechaOperacion = ahora;
        }
    }
}

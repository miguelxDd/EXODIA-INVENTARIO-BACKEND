package com.exodia.inventario.domain.modelo.contenedor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.exodia.inventario.domain.base.EntidadBase;
import com.exodia.inventario.domain.enums.TipoReferencia;
import com.exodia.inventario.domain.modelo.catalogo.Bodega;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
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
@Table(name = "inv_reservas")
public class Reserva extends EntidadBase {

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

    @Column(name = "cantidad_reservada", nullable = false, precision = 18, scale = 6)
    private BigDecimal cantidadReservada;

    @Column(name = "cantidad_cumplida", nullable = false, precision = 18, scale = 6)
    @Builder.Default
    private BigDecimal cantidadCumplida = BigDecimal.ZERO;

    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private String estado = "PENDIENTE";

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_referencia", nullable = false, length = 50)
    private TipoReferencia tipoReferencia;

    @Column(name = "referencia_id", nullable = false)
    private Long referenciaId;

    @Column(name = "referencia_linea_id")
    private Long referenciaLineaId;

    @Column(name = "fecha_expiracion")
    private OffsetDateTime fechaExpiracion;

    @Column(name = "creado_por")
    private Long creadoPor;

    @Column(name = "creado_en", updatable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "modificado_en")
    private OffsetDateTime modificadoEn;

    @Version
    @Column(name = "version")
    private Long version;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime ahora = OffsetDateTime.now();
        this.creadoEn = ahora;
        this.modificadoEn = ahora;
    }

    @PreUpdate
    protected void onUpdate() {
        this.modificadoEn = OffsetDateTime.now();
    }

    /**
     * Calcula la cantidad pendiente de cumplir en esta reserva.
     *
     * @return cantidadReservada - cantidadCumplida
     */
    public BigDecimal getCantidadPendiente() {
        return cantidadReservada.subtract(cantidadCumplida);
    }
}

package com.exodia.inventario.domain.modelo.catalogo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;

import com.exodia.inventario.domain.base.EntidadBase;
import com.exodia.inventario.domain.enums.TipoOperacionConversion;

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
import jakarta.persistence.UniqueConstraint;
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
@Table(name = "inv_conversiones", uniqueConstraints = {
        @UniqueConstraint(name = "uq_conversiones",
                columnNames = {"empresa_id", "unidad_origen_id", "unidad_destino_id", "producto_id"})
})
public class ConversionUnidad extends EntidadBase {

    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_origen_id", nullable = false)
    private Unidad unidadOrigen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_destino_id", nullable = false)
    private Unidad unidadDestino;

    @Column(name = "factor_conversion", nullable = false, precision = 18, scale = 6)
    private BigDecimal factorConversion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_operacion", nullable = false, length = 15)
    private TipoOperacionConversion tipoOperacion;

    @Column(name = "producto_id")
    private Long productoId;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "creado_en", updatable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "modificado_en")
    private OffsetDateTime modificadoEn;

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
     * Convierte una cantidad de la unidad origen a la unidad destino
     * usando el factor y tipo de operacion configurados.
     *
     * @param cantidadOrigen cantidad en la unidad de origen
     * @return cantidad convertida en la unidad de destino
     */
    public BigDecimal convertir(BigDecimal cantidadOrigen) {
        if (tipoOperacion == TipoOperacionConversion.MULTIPLICAR) {
            return cantidadOrigen.multiply(factorConversion);
        } else {
            return cantidadOrigen.divide(factorConversion, 6, RoundingMode.HALF_UP);
        }
    }
}

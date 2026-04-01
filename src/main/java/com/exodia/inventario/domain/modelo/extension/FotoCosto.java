package com.exodia.inventario.domain.modelo.extension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.exodia.inventario.domain.base.EntidadBase;
import com.exodia.inventario.domain.enums.MetodoCosto;
import com.exodia.inventario.domain.modelo.catalogo.Bodega;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;
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
@Table(name = "inv_fotos_costo")
public class FotoCosto extends EntidadBase {

    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bodega_id")
    private Bodega bodega;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_id", nullable = false)
    private Unidad unidad;

    @Column(name = "cantidad_stock", nullable = false, precision = 18, scale = 6)
    private BigDecimal cantidadStock;

    @Column(name = "costo_unitario", nullable = false, precision = 18, scale = 6)
    private BigDecimal costoUnitario;

    @Column(name = "costo_total", nullable = false, precision = 18, scale = 6)
    private BigDecimal costoTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_costo", nullable = false, length = 30)
    private MetodoCosto metodoCosto;

    @Column(name = "fecha_foto", nullable = false)
    private OffsetDateTime fechaFoto;

    @Column(name = "creado_en", updatable = false, nullable = false)
    private OffsetDateTime creadoEn;

    @PrePersist
    protected void onCreate() {
        this.creadoEn = OffsetDateTime.now();
    }
}

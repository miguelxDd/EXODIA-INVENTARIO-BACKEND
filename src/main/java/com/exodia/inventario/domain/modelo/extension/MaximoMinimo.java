package com.exodia.inventario.domain.modelo.extension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.exodia.inventario.domain.base.EntidadAuditable;
import com.exodia.inventario.domain.modelo.catalogo.Bodega;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;
import com.exodia.inventario.domain.modelo.catalogo.Unidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "inv_maximos_minimos", uniqueConstraints = {
        @UniqueConstraint(name = "uq_maxmin_empresa_producto_bodega", columnNames = {"empresa_id", "producto_id", "bodega_id"})
})
public class MaximoMinimo extends EntidadAuditable {

    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bodega_id", nullable = false)
    private Bodega bodega;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_id", nullable = false)
    private Unidad unidad;

    @Column(name = "stock_minimo", nullable = false, precision = 18, scale = 6)
    private BigDecimal stockMinimo;

    @Column(name = "stock_maximo", nullable = false, precision = 18, scale = 6)
    private BigDecimal stockMaximo;

    @Column(name = "punto_reorden", precision = 18, scale = 6)
    private BigDecimal puntoReorden;

    @Column(name = "stock_actual_calculado", precision = 18, scale = 6)
    private BigDecimal stockActualCalculado;

    @Column(name = "ultima_verificacion")
    private OffsetDateTime ultimaVerificacion;
}

package com.exodia.inventario.domain.modelo.extension;

import java.math.BigDecimal;

import com.exodia.inventario.domain.base.EntidadAuditable;
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
@Table(name = "inv_configuracion_producto", uniqueConstraints = {
        @UniqueConstraint(name = "uq_config_producto_empresa", columnNames = {"empresa_id", "producto_id"})
})
public class ConfiguracionProducto extends EntidadAuditable {

    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(name = "maneja_lote", nullable = false)
    @Builder.Default
    private Boolean manejaLote = false;

    @Column(name = "maneja_vencimiento", nullable = false)
    @Builder.Default
    private Boolean manejaVencimiento = false;

    @Column(name = "tolerancia_merma", nullable = false, precision = 18, scale = 6)
    @Builder.Default
    private BigDecimal toleranciaMerma = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_base_id")
    private Unidad unidadBase;
}

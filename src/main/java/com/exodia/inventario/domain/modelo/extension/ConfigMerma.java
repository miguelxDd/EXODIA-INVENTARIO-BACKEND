package com.exodia.inventario.domain.modelo.extension;

import java.math.BigDecimal;

import com.exodia.inventario.domain.base.EntidadAuditable;
import com.exodia.inventario.domain.enums.TipoMerma;
import com.exodia.inventario.domain.modelo.catalogo.Bodega;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "inv_config_merma")
public class ConfigMerma extends EntidadAuditable {

    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(name = "producto_id")
    private Long productoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bodega_id")
    private Bodega bodega;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_merma", nullable = false, length = 20)
    private TipoMerma tipoMerma;

    @Column(name = "porcentaje_merma", precision = 8, scale = 4)
    private BigDecimal porcentajeMerma;

    @Column(name = "cantidad_fija_merma", precision = 18, scale = 6)
    private BigDecimal cantidadFijaMerma;

    @Column(name = "frecuencia_dias")
    private Integer frecuenciaDias;
}

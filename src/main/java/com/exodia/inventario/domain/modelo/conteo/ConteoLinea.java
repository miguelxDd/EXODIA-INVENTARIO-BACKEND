package com.exodia.inventario.domain.modelo.conteo;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.exodia.inventario.domain.base.EntidadBase;
import com.exodia.inventario.domain.modelo.contenedor.Contenedor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "inv_conteo_lineas")
public class ConteoLinea extends EntidadBase {

    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conteo_fisico_id", nullable = false)
    private ConteoFisico conteoFisico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contenedor_id", nullable = false)
    private Contenedor contenedor;

    @Column(name = "cantidad_sistema", nullable = false, precision = 18, scale = 6)
    private BigDecimal cantidadSistema;

    @Column(name = "cantidad_contada", precision = 18, scale = 6)
    private BigDecimal cantidadContada;

    @Column(name = "diferencia", precision = 18, scale = 6)
    private BigDecimal diferencia;

    @Column(name = "aplicado", nullable = false)
    @Builder.Default
    private Boolean aplicado = false;

    @Column(name = "creado_en", updatable = false)
    private OffsetDateTime creadoEn;

    @PrePersist
    protected void onCreate() {
        this.creadoEn = OffsetDateTime.now();
    }
}

package com.exodia.inventario.domain.modelo.ajuste;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.exodia.inventario.domain.base.EntidadBase;
import com.exodia.inventario.domain.modelo.contenedor.Contenedor;
import com.exodia.inventario.domain.modelo.contenedor.Operacion;

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
@Table(name = "inv_ajuste_lineas")
public class AjusteLinea extends EntidadBase {

    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ajuste_id", nullable = false)
    private Ajuste ajuste;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contenedor_id", nullable = false)
    private Contenedor contenedor;

    @Column(name = "cantidad_anterior", precision = 18, scale = 6)
    private BigDecimal cantidadAnterior;

    @Column(name = "cantidad_nueva", precision = 18, scale = 6)
    private BigDecimal cantidadNueva;

    @Column(name = "cantidad_ajuste", nullable = false, precision = 18, scale = 6)
    private BigDecimal cantidadAjuste;

    @Column(name = "precio_anterior", precision = 18, scale = 6)
    private BigDecimal precioAnterior;

    @Column(name = "precio_nuevo", precision = 18, scale = 6)
    private BigDecimal precioNuevo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operacion_id")
    private Operacion operacion;

    @Column(name = "creado_en", updatable = false)
    private OffsetDateTime creadoEn;

    @PrePersist
    protected void onCreate() {
        this.creadoEn = OffsetDateTime.now();
    }
}

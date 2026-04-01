package com.exodia.inventario.domain.modelo.transferencia;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.exodia.inventario.domain.base.EntidadBase;
import com.exodia.inventario.domain.modelo.catalogo.Unidad;

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
@Table(name = "inv_transferencia_lineas")
public class TransferenciaLinea extends EntidadBase {

    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transferencia_id", nullable = false)
    private Transferencia transferencia;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_id", nullable = false)
    private Unidad unidad;

    @Column(name = "contenedor_id")
    private Long contenedorId;

    @Column(name = "cantidad_solicitada", nullable = false, precision = 18, scale = 6)
    private BigDecimal cantidadSolicitada;

    @Column(name = "cantidad_despachada", precision = 18, scale = 6)
    @Builder.Default
    private BigDecimal cantidadDespachada = BigDecimal.ZERO;

    @Column(name = "cantidad_recibida", precision = 18, scale = 6)
    @Builder.Default
    private BigDecimal cantidadRecibida = BigDecimal.ZERO;

    @Column(name = "creado_en", updatable = false)
    private OffsetDateTime creadoEn;

    @PrePersist
    protected void onCreate() {
        this.creadoEn = OffsetDateTime.now();
    }
}

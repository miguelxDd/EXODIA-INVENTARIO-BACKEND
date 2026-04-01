package com.exodia.inventario.domain.modelo.transferencia;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.exodia.inventario.domain.base.EntidadBase;
import com.exodia.inventario.domain.modelo.contenedor.Contenedor;
import com.exodia.inventario.domain.modelo.contenedor.Operacion;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
@Table(name = "inv_transferencia_contenedores")
public class TransferenciaContenedor extends EntidadBase {

    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transferencia_id", nullable = false)
    private Transferencia transferencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contenedor_id", nullable = false)
    private Contenedor contenedor;

    @Column(name = "transferencia_linea_id")
    private Long transferenciaLineaId;

    @Column(name = "cantidad", nullable = false, precision = 18, scale = 6)
    private BigDecimal cantidad;

    @Column(name = "cantidad_recibida", precision = 18, scale = 6)
    @Builder.Default
    private BigDecimal cantidadRecibida = BigDecimal.ZERO;

    @Column(name = "recibido", nullable = false)
    @Builder.Default
    private Boolean recibido = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operacion_salida_id")
    private Operacion operacionSalida;

    /**
     * Ultima operacion de entrada (backward compat con esquema original).
     * Para trazabilidad completa de recepciones parciales, usar {@link #entradas}.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operacion_entrada_id")
    private Operacion operacionEntrada;

    @OneToMany(mappedBy = "transferenciaContenedor", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TransferenciaContenedorEntrada> entradas = new ArrayList<>();

    @Column(name = "creado_en", updatable = false)
    private OffsetDateTime creadoEn;

    @PrePersist
    protected void onCreate() {
        this.creadoEn = OffsetDateTime.now();
    }
}

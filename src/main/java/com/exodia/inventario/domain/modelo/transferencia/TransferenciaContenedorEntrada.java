package com.exodia.inventario.domain.modelo.transferencia;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.exodia.inventario.domain.modelo.contenedor.Contenedor;
import com.exodia.inventario.domain.modelo.contenedor.Operacion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "inv_transferencia_contenedor_entradas")
public class TransferenciaContenedorEntrada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transferencia_contenedor_id", nullable = false)
    private TransferenciaContenedor transferenciaContenedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operacion_entrada_id", nullable = false)
    private Operacion operacionEntrada;

    @Column(name = "cantidad_recibida", nullable = false, precision = 18, scale = 6)
    private BigDecimal cantidadRecibida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contenedor_destino_id")
    private Contenedor contenedorDestino;

    @Column(name = "creado_en", updatable = false)
    private OffsetDateTime creadoEn;

    @PrePersist
    protected void onCreate() {
        this.creadoEn = OffsetDateTime.now();
    }
}

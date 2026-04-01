package com.exodia.inventario.domain.modelo.extension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.exodia.inventario.domain.base.EntidadBase;
import com.exodia.inventario.domain.enums.TipoMerma;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;
import com.exodia.inventario.domain.modelo.contenedor.Contenedor;
import com.exodia.inventario.domain.modelo.contenedor.Operacion;

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
@Table(name = "inv_registros_merma")
public class RegistroMerma extends EntidadBase {

    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contenedor_id", nullable = false)
    private Contenedor contenedor;

    @Column(name = "cantidad_merma", nullable = false, precision = 18, scale = 6)
    private BigDecimal cantidadMerma;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_merma", nullable = false, length = 20)
    private TipoMerma tipoMerma;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "config_merma_id")
    private ConfigMerma configMerma;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operacion_id")
    private Operacion operacion;

    @Column(name = "comentarios", columnDefinition = "TEXT")
    private String comentarios;

    @Column(name = "creado_en", updatable = false, nullable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "creado_por")
    private Long creadoPor;

    @PrePersist
    protected void onCreate() {
        this.creadoEn = OffsetDateTime.now();
    }
}

package com.exodia.inventario.domain.modelo.catalogo;

import java.time.OffsetDateTime;

import com.exodia.inventario.domain.base.EntidadBase;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
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
@Table(name = "inv_unidades", uniqueConstraints = {
        @UniqueConstraint(name = "uq_unidades_empresa_codigo", columnNames = {"empresa_id", "codigo"})
})
public class Unidad extends EntidadBase {

    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(name = "codigo", nullable = false, length = 20)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "abreviatura", length = 10)
    private String abreviatura;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "creado_en", updatable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "modificado_en")
    private OffsetDateTime modificadoEn;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime ahora = OffsetDateTime.now();
        this.creadoEn = ahora;
        this.modificadoEn = ahora;
    }

    @PreUpdate
    protected void onUpdate() {
        this.modificadoEn = OffsetDateTime.now();
    }
}

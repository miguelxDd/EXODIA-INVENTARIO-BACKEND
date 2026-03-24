package com.exodia.inventario.domain.modelo.catalogo;

import java.time.OffsetDateTime;

import com.exodia.inventario.domain.base.EntidadBase;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
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
@Table(name = "inv_empresas")
public class Empresa extends EntidadBase {

    private static final long serialVersionUID = 1L;

    @Column(name = "codigo", nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "nit", length = 50)
    private String nit;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "creado_en", updatable = false, nullable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "modificado_en", nullable = false)
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

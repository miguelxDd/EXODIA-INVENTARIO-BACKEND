package com.exodia.inventario.domain.modelo.catalogo;

import com.exodia.inventario.domain.base.EntidadAuditable;
import com.exodia.inventario.domain.enums.TipoUbicacion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "inv_ubicaciones", uniqueConstraints = {
        @UniqueConstraint(name = "uq_ubicaciones_bodega_codigo", columnNames = {"bodega_id", "codigo"})
})
public class Ubicacion extends EntidadAuditable {

    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bodega_id", nullable = false)
    private Bodega bodega;

    @Column(name = "codigo", nullable = false, length = 50)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "codigo_barras", length = 100)
    private String codigoBarras;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_ubicacion", nullable = false, length = 30)
    @Builder.Default
    private TipoUbicacion tipoUbicacion = TipoUbicacion.GENERAL;
}

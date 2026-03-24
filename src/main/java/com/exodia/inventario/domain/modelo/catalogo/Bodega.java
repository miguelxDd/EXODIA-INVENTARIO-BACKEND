package com.exodia.inventario.domain.modelo.catalogo;

import java.util.ArrayList;
import java.util.List;

import com.exodia.inventario.domain.base.EntidadAuditable;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
@Table(name = "inv_bodegas", uniqueConstraints = {
        @UniqueConstraint(name = "uq_bodegas_empresa_codigo", columnNames = {"empresa_id", "codigo"})
})
public class Bodega extends EntidadAuditable {

    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(name = "codigo", nullable = false, length = 30)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "direccion", length = 500)
    private String direccion;

    @Column(name = "ciudad", length = 100)
    private String ciudad;

    @Column(name = "pais", length = 100)
    private String pais;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ubicacion_standby_id")
    private Ubicacion ubicacionStandby;

    @Column(name = "es_producto_terminado", nullable = false)
    @Builder.Default
    private Boolean esProductoTerminado = false;

    @Column(name = "es_consignacion", nullable = false)
    @Builder.Default
    private Boolean esConsignacion = false;

    @OneToMany(mappedBy = "bodega", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Ubicacion> ubicaciones = new ArrayList<>();
}

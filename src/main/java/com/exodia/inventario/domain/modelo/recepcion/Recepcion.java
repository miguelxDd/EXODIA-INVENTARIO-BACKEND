package com.exodia.inventario.domain.modelo.recepcion;

import java.util.ArrayList;
import java.util.List;

import com.exodia.inventario.domain.base.EntidadAuditable;
import com.exodia.inventario.domain.enums.TipoRecepcion;
import com.exodia.inventario.domain.modelo.catalogo.Bodega;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
@Table(name = "inv_recepciones")
public class Recepcion extends EntidadAuditable {

    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(name = "numero_recepcion", nullable = false, length = 50)
    private String numeroRecepcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bodega_id", nullable = false)
    private Bodega bodega;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_recepcion", nullable = false, length = 30)
    private TipoRecepcion tipoRecepcion;

    @Column(name = "referencia_origen_id")
    private Long referenciaOrigenId;

    @Column(name = "proveedor_id")
    private Long proveedorId;

    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private String estado = "CONFIRMADO";

    @Column(name = "comentarios", columnDefinition = "TEXT")
    private String comentarios;

    @OneToMany(mappedBy = "recepcion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RecepcionLinea> lineas = new ArrayList<>();
}

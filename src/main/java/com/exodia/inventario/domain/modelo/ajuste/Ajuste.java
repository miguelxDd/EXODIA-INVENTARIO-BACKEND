package com.exodia.inventario.domain.modelo.ajuste;

import java.util.ArrayList;
import java.util.List;

import com.exodia.inventario.domain.base.EntidadAuditable;
import com.exodia.inventario.domain.enums.TipoReferencia;
import com.exodia.inventario.domain.modelo.catalogo.Bodega;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;
import com.exodia.inventario.domain.modelo.catalogo.TipoAjuste;

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
@Table(name = "inv_ajustes")
public class Ajuste extends EntidadAuditable {

    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(name = "numero_ajuste", nullable = false, length = 50)
    private String numeroAjuste;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bodega_id", nullable = false)
    private Bodega bodega;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_ajuste_id", nullable = false)
    private TipoAjuste tipoAjuste;

    @Column(name = "motivo", columnDefinition = "TEXT")
    private String motivo;

    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private String estado = "APLICADO";

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_referencia", length = 50)
    private TipoReferencia tipoReferencia;

    @Column(name = "referencia_id")
    private Long referenciaId;

    @OneToMany(mappedBy = "ajuste", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AjusteLinea> lineas = new ArrayList<>();
}

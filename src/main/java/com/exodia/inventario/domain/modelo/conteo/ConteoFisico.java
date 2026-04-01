package com.exodia.inventario.domain.modelo.conteo;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.exodia.inventario.domain.base.EntidadAuditable;
import com.exodia.inventario.domain.modelo.ajuste.Ajuste;
import com.exodia.inventario.domain.modelo.catalogo.Bodega;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "inv_conteos_fisicos")
public class ConteoFisico extends EntidadAuditable {

    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(name = "numero_conteo", nullable = false, length = 50)
    private String numeroConteo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bodega_id", nullable = false)
    private Bodega bodega;

    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private String estado = "EN_PROGRESO";

    @Column(name = "fecha_conteo", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaConteo = OffsetDateTime.now();

    @Column(name = "comentarios", columnDefinition = "TEXT")
    private String comentarios;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ajuste_generado_id")
    private Ajuste ajusteGenerado;

    @OneToMany(mappedBy = "conteoFisico", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ConteoLinea> lineas = new ArrayList<>();
}

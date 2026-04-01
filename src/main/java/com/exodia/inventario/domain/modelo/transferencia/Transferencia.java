package com.exodia.inventario.domain.modelo.transferencia;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.exodia.inventario.domain.base.EntidadAuditable;
import com.exodia.inventario.domain.enums.TipoTransferencia;
import com.exodia.inventario.domain.modelo.catalogo.Bodega;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;
import com.exodia.inventario.domain.modelo.catalogo.EstadoTransferencia;

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
@Table(name = "inv_transferencias")
public class Transferencia extends EntidadAuditable {

    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(name = "numero_transferencia", nullable = false, length = 50)
    private String numeroTransferencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_transferencia", nullable = false, length = 30)
    private TipoTransferencia tipoTransferencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bodega_origen_id", nullable = false)
    private Bodega bodegaOrigen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bodega_destino_id", nullable = false)
    private Bodega bodegaDestino;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estado_id", nullable = false)
    private EstadoTransferencia estadoTransferencia;

    @Column(name = "comentarios", columnDefinition = "TEXT")
    private String comentarios;

    @Column(name = "fecha_despacho")
    private OffsetDateTime fechaDespacho;

    @Column(name = "fecha_recepcion")
    private OffsetDateTime fechaRecepcion;

    @OneToMany(mappedBy = "transferencia", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TransferenciaLinea> lineas = new ArrayList<>();

    @OneToMany(mappedBy = "transferencia", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TransferenciaContenedor> contenedores = new ArrayList<>();
}

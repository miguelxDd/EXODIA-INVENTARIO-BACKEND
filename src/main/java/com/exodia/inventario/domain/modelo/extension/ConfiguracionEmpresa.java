package com.exodia.inventario.domain.modelo.extension;

import com.exodia.inventario.domain.base.EntidadAuditable;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "inv_configuracion_empresa", uniqueConstraints = {
        @UniqueConstraint(name = "uq_config_empresa", columnNames = {"empresa_id"})
})
public class ConfiguracionEmpresa extends EntidadAuditable {

    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(name = "expiracion_reserva_horas", nullable = false)
    @Builder.Default
    private Integer expiracionReservaHoras = 48;

    @Column(name = "dias_alerta_vencimiento", nullable = false)
    @Builder.Default
    private Integer diasAlertaVencimiento = 90;

    @Column(name = "barcode_prefijo", nullable = false, length = 20)
    @Builder.Default
    private String barcodePrefijo = "INV";

    @Column(name = "barcode_longitud_padding", nullable = false)
    @Builder.Default
    private Integer barcodeLongitudPadding = 8;

    @Column(name = "politica_salida", nullable = false, length = 20)
    @Builder.Default
    private String politicaSalida = "FEFO";
}

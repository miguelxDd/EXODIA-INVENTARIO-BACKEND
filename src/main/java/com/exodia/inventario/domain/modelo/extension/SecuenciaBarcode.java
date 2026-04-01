package com.exodia.inventario.domain.modelo.extension;

import com.exodia.inventario.domain.base.EntidadBase;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
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
@Table(name = "inv_secuencias_barcode", uniqueConstraints = {
        @UniqueConstraint(name = "uq_secuencias_empresa_prefijo", columnNames = {"empresa_id", "prefijo"})
})
public class SecuenciaBarcode extends EntidadBase {

    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(name = "prefijo", nullable = false, length = 20)
    private String prefijo;

    @Column(name = "ultimo_valor", nullable = false)
    @Builder.Default
    private Long ultimoValor = 0L;

    @Column(name = "longitud_padding", nullable = false)
    @Builder.Default
    private Integer longitudPadding = 8;

    @Version
    @Column(name = "version")
    private Long version;
}

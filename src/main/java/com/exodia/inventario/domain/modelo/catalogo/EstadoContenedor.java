package com.exodia.inventario.domain.modelo.catalogo;

import com.exodia.inventario.domain.base.EntidadBase;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "inv_estados_contenedor")
public class EstadoContenedor extends EntidadBase {

    private static final long serialVersionUID = 1L;

    @Column(name = "codigo", nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "permite_picking", nullable = false)
    @Builder.Default
    private Boolean permitePicking = false;

    @Column(name = "permite_transferencia", nullable = false)
    @Builder.Default
    private Boolean permiteTransferencia = false;
}

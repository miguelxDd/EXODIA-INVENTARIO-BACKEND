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
@Table(name = "inv_tipos_operacion")
public class TipoOperacion extends EntidadBase {

    private static final long serialVersionUID = 1L;

    @Column(name = "codigo", nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    /**
     * Signo de la operacion: 1 = entrada, -1 = salida, 0 = informativo.
     */
    @Column(name = "signo", nullable = false)
    private Short signo;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;
}

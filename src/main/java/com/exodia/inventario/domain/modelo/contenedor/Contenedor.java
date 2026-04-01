package com.exodia.inventario.domain.modelo.contenedor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.exodia.inventario.domain.base.EntidadAuditable;
import com.exodia.inventario.domain.modelo.catalogo.Bodega;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;
import com.exodia.inventario.domain.modelo.catalogo.EstadoContenedor;
import com.exodia.inventario.domain.modelo.catalogo.Ubicacion;
import com.exodia.inventario.domain.modelo.catalogo.Unidad;

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
@Table(name = "inv_contenedores")
public class Contenedor extends EntidadAuditable {

    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(name = "codigo_barras", nullable = false, length = 100)
    private String codigoBarras;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(name = "proveedor_id")
    private Long proveedorId;

    @Column(name = "producto_proveedor_id")
    private Long productoProveedorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_id", nullable = false)
    private Unidad unidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bodega_id", nullable = false)
    private Bodega bodega;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ubicacion_id", nullable = false)
    private Ubicacion ubicacion;

    @Column(name = "precio_unitario", nullable = false, precision = 18, scale = 6)
    @Builder.Default
    private BigDecimal precioUnitario = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id")
    private Lote lote;

    @Column(name = "numero_lote", length = 100)
    private String numeroLote;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "numero_serie", length = 100)
    private String numeroSerie;

    @Column(name = "marca_id")
    private Long marcaId;

    @Column(name = "origen_id")
    private Long origenId;

    @Column(name = "info_garantia", length = 500)
    private String infoGarantia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estado_id", nullable = false)
    private EstadoContenedor estado;

    @OneToMany(mappedBy = "contenedor", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Operacion> operaciones = new ArrayList<>();
}

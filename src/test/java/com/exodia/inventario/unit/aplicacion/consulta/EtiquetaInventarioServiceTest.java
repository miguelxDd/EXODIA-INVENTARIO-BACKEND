package com.exodia.inventario.unit.aplicacion.consulta;

import com.exodia.inventario.aplicacion.consulta.StockQueryService;
import com.exodia.inventario.aplicacion.consulta.impl.EtiquetaInventarioServiceImpl;
import com.exodia.inventario.domain.enums.TipoUbicacion;
import com.exodia.inventario.domain.modelo.catalogo.Bodega;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;
import com.exodia.inventario.domain.modelo.catalogo.EstadoContenedor;
import com.exodia.inventario.domain.modelo.catalogo.Ubicacion;
import com.exodia.inventario.domain.modelo.catalogo.Unidad;
import com.exodia.inventario.domain.modelo.contenedor.Contenedor;
import com.exodia.inventario.interfaz.dto.respuesta.EtiquetaResponse;
import com.exodia.inventario.repositorio.catalogo.UbicacionRepository;
import com.exodia.inventario.repositorio.contenedor.ContenedorRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class EtiquetaInventarioServiceTest {

    @Mock private ContenedorRepository contenedorRepository;
    @Mock private UbicacionRepository ubicacionRepository;
    @Mock private StockQueryService stockQueryService;

    @InjectMocks
    private EtiquetaInventarioServiceImpl etiquetaInventarioService;

    @Test
    void deberiaGenerarEtiquetaDeContenedorConZplYSvg() {
        Empresa empresa = Empresa.builder().codigo("EMP").nombre("Empresa").build();
        empresa.setId(1L);

        Bodega bodega = Bodega.builder().empresa(empresa).codigo("BOD1").nombre("Principal").build();
        bodega.setId(10L);

        Unidad unidad = Unidad.builder().empresa(empresa).nombre("Unidad").abreviatura("UND").build();
        unidad.setId(2L);

        Ubicacion ubicacion = Ubicacion.builder()
                .bodega(bodega)
                .codigo("A-01")
                .nombre("Rack A-01")
                .tipoUbicacion(TipoUbicacion.GENERAL)
                .build();
        ubicacion.setId(5L);

        EstadoContenedor estado = EstadoContenedor.builder().codigo("DISPONIBLE").nombre("Disponible").build();
        estado.setId(1L);

        Contenedor contenedor = Contenedor.builder()
                .empresa(empresa)
                .codigoBarras("INV000123")
                .productoId(100L)
                .unidad(unidad)
                .bodega(bodega)
                .ubicacion(ubicacion)
                .numeroLote("L001")
                .fechaVencimiento(LocalDate.parse("2026-05-01"))
                .estado(estado)
                .build();
        contenedor.setId(99L);
        contenedor.setActivo(true);

        when(contenedorRepository.findById(99L)).thenReturn(Optional.of(contenedor));
        when(stockQueryService.obtenerStockContenedorPorEmpresa(1L, 99L))
                .thenReturn(new BigDecimal("12.500"));

        EtiquetaResponse response = etiquetaInventarioService.generarEtiquetaContenedor(1L, 99L);

        assertEquals("CONTENEDOR", response.tipoEtiqueta());
        assertEquals("INV000123", response.codigoBarras());
        assertTrue(response.zpl().contains("^FDINV000123^FS"));
        assertTrue(response.svgVistaPrevia().contains("INV000123"));
        assertTrue(response.detalles().contains("Stock: 12.5"));
    }

    @Test
    void deberiaUsarCodigoDeUbicacionComoBarcodeSiNoTieneUnoPropio() {
        Empresa empresa = Empresa.builder().codigo("EMP").nombre("Empresa").build();
        empresa.setId(1L);

        Bodega bodega = Bodega.builder().empresa(empresa).codigo("BOD1").nombre("Principal").build();
        bodega.setId(10L);

        Ubicacion ubicacion = Ubicacion.builder()
                .bodega(bodega)
                .codigo("UBI-001")
                .nombre("Archivo 1")
                .tipoUbicacion(TipoUbicacion.RECEPCION)
                .build();
        ubicacion.setId(7L);
        ubicacion.setActivo(true);

        when(ubicacionRepository.findById(7L)).thenReturn(Optional.of(ubicacion));

        EtiquetaResponse response = etiquetaInventarioService.generarEtiquetaUbicacion(1L, 7L);

        assertEquals("UBI-001", response.codigoBarras());
        assertTrue(response.zpl().contains("^FDUBI-001^FS"));
    }
}

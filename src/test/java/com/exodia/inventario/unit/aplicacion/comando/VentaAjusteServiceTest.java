package com.exodia.inventario.unit.aplicacion.comando;

import com.exodia.inventario.aplicacion.comando.BarcodeService;
import com.exodia.inventario.aplicacion.comando.ConfiguracionEmpresaService;
import com.exodia.inventario.aplicacion.comando.OperacionService;
import com.exodia.inventario.aplicacion.comando.impl.VentaAjusteServiceImpl;
import com.exodia.inventario.aplicacion.consulta.StockQueryService;
import com.exodia.inventario.domain.enums.TipoOperacionCodigo;
import com.exodia.inventario.domain.enums.TipoReferencia;
import com.exodia.inventario.domain.modelo.ajuste.Ajuste;
import com.exodia.inventario.domain.modelo.catalogo.Bodega;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;
import com.exodia.inventario.domain.modelo.catalogo.EstadoContenedor;
import com.exodia.inventario.domain.modelo.catalogo.TipoAjuste;
import com.exodia.inventario.domain.modelo.catalogo.Unidad;
import com.exodia.inventario.domain.modelo.contenedor.Contenedor;
import com.exodia.inventario.domain.modelo.contenedor.Operacion;
import com.exodia.inventario.domain.modelo.extension.ConfiguracionEmpresa;
import com.exodia.inventario.domain.politica.PoliticaDeduccionStock;
import com.exodia.inventario.domain.servicio.PoliticaFEFO;
import com.exodia.inventario.excepcion.OperacionInvalidaException;
import com.exodia.inventario.infraestructura.integracion.VentasAdapter;
import com.exodia.inventario.interfaz.dto.peticion.AjusteVentaLineaRequest;
import com.exodia.inventario.interfaz.dto.peticion.CrearAjusteVentaRequest;
import com.exodia.inventario.interfaz.dto.respuesta.AjusteResponse;
import com.exodia.inventario.interfaz.mapeador.AjusteMapeador;
import com.exodia.inventario.repositorio.ajuste.AjusteRepository;
import com.exodia.inventario.repositorio.catalogo.BodegaRepository;
import com.exodia.inventario.repositorio.catalogo.EmpresaRepository;
import com.exodia.inventario.repositorio.catalogo.EstadoContenedorRepository;
import com.exodia.inventario.repositorio.catalogo.TipoAjusteRepository;
import com.exodia.inventario.repositorio.catalogo.UnidadRepository;
import com.exodia.inventario.repositorio.contenedor.ContenedorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class VentaAjusteServiceTest {

    @Mock private AjusteRepository ajusteRepository;
    @Mock private EmpresaRepository empresaRepository;
    @Mock private BodegaRepository bodegaRepository;
    @Mock private TipoAjusteRepository tipoAjusteRepository;
    @Mock private UnidadRepository unidadRepository;
    @Mock private EstadoContenedorRepository estadoContenedorRepository;
    @Mock private ContenedorRepository contenedorRepository;
    @Mock private OperacionService operacionService;
    @Mock private StockQueryService stockQueryService;
    @Mock private BarcodeService barcodeService;
    @Mock private ConfiguracionEmpresaService configuracionEmpresaService;
    @Mock private PoliticaFEFO politicaFEFO;
    @Mock private PoliticaDeduccionStock politicaDeduccionStock;
    @Mock private VentasAdapter ventasAdapter;
    @Mock private AjusteMapeador ajusteMapeador;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private VentaAjusteServiceImpl ventaAjusteService;

    private Empresa empresa;
    private Bodega bodega;
    private Unidad unidad;
    private Contenedor contenedor;

    @BeforeEach
    void setUp() {
        empresa = Empresa.builder().codigo("EMP1").nombre("Empresa").build();
        empresa.setId(1L);

        bodega = Bodega.builder().empresa(empresa).codigo("BOD1").nombre("Principal").build();
        bodega.setId(1L);
        bodega.setActivo(true);

        unidad = Unidad.builder().empresa(empresa).nombre("Unidad").abreviatura("UND").build();
        unidad.setId(1L);
        unidad.setActivo(true);

        EstadoContenedor estadoDisponible = EstadoContenedor.builder()
                .codigo("DISPONIBLE").nombre("Disponible").build();
        estadoDisponible.setId(1L);

        contenedor = Contenedor.builder()
                .empresa(empresa)
                .bodega(bodega)
                .unidad(unidad)
                .productoId(100L)
                .precioUnitario(new BigDecimal("5.50"))
                .codigoBarras("INV001")
                .estado(estadoDisponible)
                .build();
        contenedor.setId(10L);
    }

    @Test
    void deberiaCrearAjustePorVentaFacturadaConPoliticaManual() {
        AjusteVentaLineaRequest linea = new AjusteVentaLineaRequest(
                100L, 1L, new BigDecimal("3"), 10L, 1000L);
        CrearAjusteVentaRequest request = new CrearAjusteVentaRequest(
                1L, 900L, "Factura emitida", List.of(linea));

        TipoAjuste tipoAjuste = TipoAjuste.builder()
                .codigo("VENTA_FACTURADA")
                .nombre("Ajuste por venta facturada")
                .build();
        tipoAjuste.setId(5L);

        ConfiguracionEmpresa configuracion = ConfiguracionEmpresa.builder()
                .politicaSalida("MANUAL")
                .build();

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(bodegaRepository.findById(1L)).thenReturn(Optional.of(bodega));
        when(tipoAjusteRepository.findByCodigo("VENTA_FACTURADA")).thenReturn(Optional.of(tipoAjuste));
        doNothing().when(ventasAdapter).validarVentaFacturada(eq(1L), eq(900L), any());
        when(configuracionEmpresaService.obtenerEntidadOCrear(1L)).thenReturn(configuracion);
        when(barcodeService.generarBarcode(1L, "VTA")).thenReturn("VTA00000001");
        when(unidadRepository.findById(1L)).thenReturn(Optional.of(unidad));
        when(ajusteRepository.save(any(Ajuste.class))).thenAnswer(inv -> {
            Ajuste ajuste = inv.getArgument(0);
            if (ajuste.getId() == null) {
                ajuste.setId(1L);
            }
            return ajuste;
        });
        when(contenedorRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(contenedor));
        when(stockQueryService.obtenerStockContenedor(10L)).thenReturn(new BigDecimal("8"));
        when(stockQueryService.obtenerStockDisponible(10L)).thenReturn(new BigDecimal("8"));
        when(politicaDeduccionStock.evaluar(any(), any(), any()))
                .thenReturn(new PoliticaDeduccionStock.ResultadoValidacion(true, "OK"));
        Operacion operacion = Operacion.builder().build();
        operacion.setId(7L);
        when(operacionService.crearOperacion(any(), eq(TipoOperacionCodigo.AJUSTE_VENTA),
                eq(new BigDecimal("3")), any(), eq(TipoReferencia.VENTA), eq(900L), eq(1000L)))
                .thenReturn(operacion);
        when(ajusteMapeador.toResponse(any(Ajuste.class)))
                .thenReturn(new AjusteResponse(1L, "VTA00000001", 1L,
                        "VENTA_FACTURADA", "Ajuste por venta facturada",
                        "Ajuste por venta facturada 900: Factura emitida",
                        "APLICADO", List.of(), null));

        AjusteResponse response = ventaAjusteService.crear(1L, request);

        assertNotNull(response);
        assertEquals("VTA00000001", response.numeroAjuste());
        verify(ventasAdapter).validarVentaFacturada(eq(1L), eq(900L), any());
        verify(eventPublisher, atLeastOnce()).publishEvent(any());
    }

    @Test
    void deberiaFallarSiPoliticaManualNoIncluyeContenedor() {
        AjusteVentaLineaRequest linea = new AjusteVentaLineaRequest(
                100L, 1L, new BigDecimal("3"), null, null);
        CrearAjusteVentaRequest request = new CrearAjusteVentaRequest(
                1L, 900L, null, List.of(linea));

        TipoAjuste tipoAjuste = TipoAjuste.builder()
                .codigo("VENTA_FACTURADA")
                .nombre("Ajuste por venta facturada")
                .build();

        ConfiguracionEmpresa configuracion = ConfiguracionEmpresa.builder()
                .politicaSalida("MANUAL")
                .build();

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(bodegaRepository.findById(1L)).thenReturn(Optional.of(bodega));
        when(tipoAjusteRepository.findByCodigo("VENTA_FACTURADA")).thenReturn(Optional.of(tipoAjuste));
        doNothing().when(ventasAdapter).validarVentaFacturada(eq(1L), eq(900L), any());
        when(configuracionEmpresaService.obtenerEntidadOCrear(1L)).thenReturn(configuracion);
        when(barcodeService.generarBarcode(1L, "VTA")).thenReturn("VTA00000001");
        when(ajusteRepository.save(any(Ajuste.class))).thenAnswer(inv -> {
            Ajuste ajuste = inv.getArgument(0);
            ajuste.setId(1L);
            return ajuste;
        });
        when(unidadRepository.findById(1L)).thenReturn(Optional.of(unidad));

        assertThrows(OperacionInvalidaException.class,
                () -> ventaAjusteService.crear(1L, request));
    }
}

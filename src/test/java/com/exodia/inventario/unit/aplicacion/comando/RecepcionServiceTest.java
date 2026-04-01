package com.exodia.inventario.unit.aplicacion.comando;

import com.exodia.inventario.aplicacion.comando.BarcodeService;
import com.exodia.inventario.aplicacion.comando.LoteService;
import com.exodia.inventario.aplicacion.comando.MermaService;
import com.exodia.inventario.aplicacion.comando.OperacionService;
import com.exodia.inventario.aplicacion.comando.impl.RecepcionServiceImpl;
import com.exodia.inventario.domain.enums.TipoOperacionCodigo;
import com.exodia.inventario.domain.enums.TipoReferencia;
import com.exodia.inventario.domain.modelo.catalogo.*;
import com.exodia.inventario.domain.modelo.contenedor.Contenedor;
import com.exodia.inventario.domain.modelo.contenedor.Operacion;
import com.exodia.inventario.domain.modelo.recepcion.Recepcion;
import com.exodia.inventario.excepcion.EntidadNoEncontradaException;
import com.exodia.inventario.interfaz.dto.peticion.CrearRecepcionRequest;
import com.exodia.inventario.interfaz.dto.peticion.RecepcionLineaRequest;
import com.exodia.inventario.interfaz.dto.respuesta.RecepcionResponse;
import com.exodia.inventario.interfaz.mapeador.RecepcionMapeador;
import com.exodia.inventario.domain.enums.EstadoContenedorCodigo;
import com.exodia.inventario.repositorio.catalogo.BodegaRepository;
import com.exodia.inventario.repositorio.catalogo.EmpresaRepository;
import com.exodia.inventario.repositorio.catalogo.EstadoContenedorRepository;
import com.exodia.inventario.repositorio.catalogo.UnidadRepository;
import com.exodia.inventario.repositorio.catalogo.UbicacionRepository;
import com.exodia.inventario.repositorio.contenedor.ContenedorRepository;
import com.exodia.inventario.infraestructura.integracion.ComprasAdapter;
import com.exodia.inventario.infraestructura.integracion.ProduccionAdapter;
import com.exodia.inventario.infraestructura.integracion.ProductoAdapter;
import com.exodia.inventario.repositorio.extension.ConfiguracionProductoRepository;
import com.exodia.inventario.repositorio.recepcion.RecepcionRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class RecepcionServiceTest {

    @Mock private RecepcionRepository recepcionRepository;
    @Mock private EmpresaRepository empresaRepository;
    @Mock private BodegaRepository bodegaRepository;
    @Mock private UnidadRepository unidadRepository;
    @Mock private UbicacionRepository ubicacionRepository;
    @Mock private ContenedorRepository contenedorRepository;
    @Mock private EstadoContenedorRepository estadoContenedorRepository;
    @Mock private OperacionService operacionService;
    @Mock private BarcodeService barcodeService;
    @Mock private LoteService loteService;
    @Mock private MermaService mermaService;
    @Mock private ConfiguracionProductoRepository configuracionProductoRepository;
    @Mock private ProductoAdapter productoAdapter;
    @Mock private ComprasAdapter comprasAdapter;
    @Mock private ProduccionAdapter produccionAdapter;
    @Mock private RecepcionMapeador recepcionMapeador;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private RecepcionServiceImpl recepcionService;

    private Empresa empresa;
    private Bodega bodega;
    private Unidad unidad;
    private Ubicacion ubicacion;

    @BeforeEach
    void setUp() {
        empresa = Empresa.builder().codigo("EMP1").nombre("Test").build();
        empresa.setId(1L);

        bodega = Bodega.builder().empresa(empresa).codigo("BOD1").nombre("Bodega").build();
        bodega.setId(1L);

        unidad = Unidad.builder().empresa(empresa).nombre("Unidad").abreviatura("UND").build();
        unidad.setId(1L);
        unidad.setActivo(true);

        ubicacion = Ubicacion.builder().bodega(bodega).nombre("Ubicacion").build();
        ubicacion.setId(1L);
        ubicacion.setActivo(true);
    }

    @Test
    void deberiaCrearRecepcionExitosamente() {
        RecepcionLineaRequest lineaReq = new RecepcionLineaRequest(
                100L, 1L, 1L, new BigDecimal("10"), null,
                new BigDecimal("50.00"), null, null, null, null);

        CrearRecepcionRequest request = new CrearRecepcionRequest(
                1L, "MANUAL", null, null, null, List.of(lineaReq));

        EstadoContenedor estadoDisponible = EstadoContenedor.builder()
                .codigo(EstadoContenedorCodigo.DISPONIBLE.getCodigo())
                .nombre("Disponible").build();
        estadoDisponible.setId(1L);

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(bodegaRepository.findById(1L)).thenReturn(Optional.of(bodega));
        when(estadoContenedorRepository.findByCodigo(EstadoContenedorCodigo.DISPONIBLE.getCodigo()))
                .thenReturn(Optional.of(estadoDisponible));
        when(productoAdapter.existeProducto(1L, 100L)).thenReturn(true);
        when(unidadRepository.findById(1L)).thenReturn(Optional.of(unidad));
        when(ubicacionRepository.findById(1L)).thenReturn(Optional.of(ubicacion));
        when(barcodeService.generarBarcode(eq(1L), eq("REC"))).thenReturn("REC00000001");
        when(barcodeService.generarBarcode(eq(1L))).thenReturn("INV00000001");
        when(contenedorRepository.save(any(Contenedor.class))).thenAnswer(inv -> {
            Contenedor c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });
        when(operacionService.crearOperacion(any(), eq(TipoOperacionCodigo.RECEPCION),
                any(), any(), eq(TipoReferencia.RECEPCION), any(), any()))
                .thenReturn(Operacion.builder().build());
        when(recepcionRepository.save(any(Recepcion.class))).thenAnswer(inv -> {
            Recepcion r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });
        when(recepcionMapeador.toResponse(any(Recepcion.class)))
                .thenReturn(new RecepcionResponse(1L, "REC00000001", 1L, "MANUAL",
                        null, null, "CONFIRMADO", null, List.of(), null));

        RecepcionResponse response = recepcionService.crear(1L, request);

        assertNotNull(response);
        assertEquals("REC00000001", response.numeroRecepcion());
        verify(operacionService).crearOperacion(any(), eq(TipoOperacionCodigo.RECEPCION),
                any(), any(), eq(TipoReferencia.RECEPCION), any(), any());
        verifyNoInteractions(mermaService);
        verify(eventPublisher).publishEvent(any(Object.class));
    }

    @Test
    void deberiaRegistrarMermaAutomaticaSiLaLineaLaIncluye() {
        RecepcionLineaRequest lineaReq = new RecepcionLineaRequest(
                100L, 1L, 1L, new BigDecimal("10"), new BigDecimal("2"),
                new BigDecimal("50.00"), null, null, null, null);

        CrearRecepcionRequest request = new CrearRecepcionRequest(
                1L, "MANUAL", null, null, null, List.of(lineaReq));

        EstadoContenedor estadoDisponible = EstadoContenedor.builder()
                .codigo(EstadoContenedorCodigo.DISPONIBLE.getCodigo())
                .nombre("Disponible").build();
        estadoDisponible.setId(1L);

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(bodegaRepository.findById(1L)).thenReturn(Optional.of(bodega));
        when(estadoContenedorRepository.findByCodigo(EstadoContenedorCodigo.DISPONIBLE.getCodigo()))
                .thenReturn(Optional.of(estadoDisponible));
        when(productoAdapter.existeProducto(1L, 100L)).thenReturn(true);
        when(unidadRepository.findById(1L)).thenReturn(Optional.of(unidad));
        when(ubicacionRepository.findById(1L)).thenReturn(Optional.of(ubicacion));
        when(barcodeService.generarBarcode(eq(1L), eq("REC"))).thenReturn("REC00000001");
        when(barcodeService.generarBarcode(eq(1L))).thenReturn("INV00000001");
        when(contenedorRepository.save(any(Contenedor.class))).thenAnswer(inv -> {
            Contenedor c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });
        when(operacionService.crearOperacion(any(), eq(TipoOperacionCodigo.RECEPCION),
                any(), any(), eq(TipoReferencia.RECEPCION), any(), any()))
                .thenReturn(Operacion.builder().build());
        when(recepcionRepository.save(any(Recepcion.class))).thenAnswer(inv -> {
            Recepcion r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });
        when(recepcionMapeador.toResponse(any(Recepcion.class)))
                .thenReturn(new RecepcionResponse(1L, "REC00000001", 1L, "MANUAL",
                        null, null, "CONFIRMADO", null, List.of(), null));

        RecepcionResponse response = recepcionService.crear(1L, request);

        assertNotNull(response);
        verify(mermaService).registrarAutomaticaEnRecepcion(
                eq(1L), eq(1L), eq(new BigDecimal("2")), contains("Recepcion REC00000001"));
    }

    @Test
    void deberiaFallarSiEmpresaNoExiste() {
        when(empresaRepository.findById(999L)).thenReturn(Optional.empty());

        CrearRecepcionRequest request = new CrearRecepcionRequest(1L, "MANUAL", null, null, null, List.of());

        assertThrows(EntidadNoEncontradaException.class,
                () -> recepcionService.crear(999L, request));
    }

    @Test
    void deberiaFallarSiBodegaNoExisteONoEsDeLaEmpresa() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(bodegaRepository.findById(999L)).thenReturn(Optional.empty());

        CrearRecepcionRequest request = new CrearRecepcionRequest(999L, "MANUAL", null, null, null, List.of());

        assertThrows(EntidadNoEncontradaException.class,
                () -> recepcionService.crear(1L, request));
    }
}

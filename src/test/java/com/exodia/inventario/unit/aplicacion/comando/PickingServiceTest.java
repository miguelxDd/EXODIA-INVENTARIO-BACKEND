package com.exodia.inventario.unit.aplicacion.comando;

import com.exodia.inventario.aplicacion.comando.BarcodeService;
import com.exodia.inventario.aplicacion.comando.ConfiguracionEmpresaService;
import com.exodia.inventario.aplicacion.comando.OperacionService;
import com.exodia.inventario.aplicacion.comando.impl.PickingServiceImpl;
import com.exodia.inventario.aplicacion.consulta.StockQueryService;
import com.exodia.inventario.domain.modelo.catalogo.*;
import com.exodia.inventario.domain.modelo.picking.OrdenPicking;
import com.exodia.inventario.domain.servicio.PoliticaFEFO;
import com.exodia.inventario.domain.politica.PoliticaDeduccionStock;
import com.exodia.inventario.excepcion.EntidadNoEncontradaException;
import com.exodia.inventario.excepcion.OperacionInvalidaException;
import com.exodia.inventario.interfaz.dto.peticion.CrearOrdenPickingRequest;
import com.exodia.inventario.interfaz.dto.peticion.PickingLineaRequest;
import com.exodia.inventario.interfaz.dto.respuesta.OrdenPickingResponse;
import com.exodia.inventario.interfaz.mapeador.PickingMapeador;
import com.exodia.inventario.repositorio.catalogo.BodegaRepository;
import com.exodia.inventario.repositorio.catalogo.EmpresaRepository;
import com.exodia.inventario.repositorio.catalogo.EstadoContenedorRepository;
import com.exodia.inventario.repositorio.catalogo.UnidadRepository;
import com.exodia.inventario.repositorio.contenedor.ContenedorRepository;
import com.exodia.inventario.repositorio.picking.OrdenPickingRepository;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class PickingServiceTest {

    @Mock private OrdenPickingRepository ordenPickingRepository;
    @Mock private EmpresaRepository empresaRepository;
    @Mock private BodegaRepository bodegaRepository;
    @Mock private UnidadRepository unidadRepository;
    @Mock private ContenedorRepository contenedorRepository;
    @Mock private EstadoContenedorRepository estadoContenedorRepository;
    @Mock private OperacionService operacionService;
    @Mock private StockQueryService stockQueryService;
    @Mock private BarcodeService barcodeService;
    @Mock private ConfiguracionEmpresaService configuracionEmpresaService;
    @Mock private PoliticaFEFO politicaFEFO;
    @Mock private PoliticaDeduccionStock politicaDeduccionStock;
    @Mock private PickingMapeador pickingMapeador;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PickingServiceImpl pickingService;

    private Empresa empresa;
    private Bodega bodega;

    @BeforeEach
    void setUp() {
        empresa = Empresa.builder().codigo("EMP1").nombre("Test").build();
        empresa.setId(1L);

        bodega = Bodega.builder().empresa(empresa).codigo("BOD1").nombre("Bodega").build();
        bodega.setId(1L);
        bodega.setActivo(true);
    }

    @Test
    void deberiaCrearOrdenPicking() {
        Unidad unidad = Unidad.builder().empresa(empresa).nombre("UND").abreviatura("UND").build();
        unidad.setId(1L);
        unidad.setActivo(true);

        PickingLineaRequest lineaReq = new PickingLineaRequest(100L, 1L, new BigDecimal("5"));
        CrearOrdenPickingRequest request = new CrearOrdenPickingRequest(
                1L, com.exodia.inventario.domain.enums.TipoPicking.GENERAL,
                null, null, "Test", List.of(lineaReq));

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(bodegaRepository.findById(1L)).thenReturn(Optional.of(bodega));
        when(barcodeService.generarBarcode(1L, "PKG")).thenReturn("PKG00000001");
        when(unidadRepository.findById(1L)).thenReturn(Optional.of(unidad));
        when(ordenPickingRepository.save(any(OrdenPicking.class))).thenAnswer(inv -> {
            OrdenPicking o = inv.getArgument(0);
            o.setId(1L);
            return o;
        });
        when(pickingMapeador.toResponse(any(OrdenPicking.class)))
                .thenReturn(new OrdenPickingResponse(1L, "PKG00000001", 1L, "GENERAL",
                        null, null, "PENDIENTE", "Test", List.of(), null));

        OrdenPickingResponse response = pickingService.crear(1L, request);

        assertNotNull(response);
        assertEquals("PENDIENTE", response.estado());
    }

    @Test
    void deberiaFallarAlCancelarOrdenNoEnPendiente() {
        OrdenPicking orden = OrdenPicking.builder()
                .empresa(empresa).bodega(bodega).estado("COMPLETADO").build();
        orden.setId(1L);

        when(ordenPickingRepository.findById(1L)).thenReturn(Optional.of(orden));

        assertThrows(OperacionInvalidaException.class,
                () -> pickingService.cancelar(1L, 1L));
    }

    @Test
    void deberiaFallarAlEjecutarOrdenNoEnPendiente() {
        OrdenPicking orden = OrdenPicking.builder()
                .empresa(empresa).bodega(bodega).estado("COMPLETADO").build();
        orden.setId(1L);

        when(ordenPickingRepository.findById(1L)).thenReturn(Optional.of(orden));

        assertThrows(OperacionInvalidaException.class,
                () -> pickingService.ejecutar(1L, 1L));
    }

    @Test
    void deberiaFallarSiEmpresaNoExiste() {
        when(empresaRepository.findById(999L)).thenReturn(Optional.empty());

        CrearOrdenPickingRequest request = new CrearOrdenPickingRequest(
                1L, com.exodia.inventario.domain.enums.TipoPicking.GENERAL,
                null, null, null, List.of());

        assertThrows(EntidadNoEncontradaException.class,
                () -> pickingService.crear(999L, request));
    }
}

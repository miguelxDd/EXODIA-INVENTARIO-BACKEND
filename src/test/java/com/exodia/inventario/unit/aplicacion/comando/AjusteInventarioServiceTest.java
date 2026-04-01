package com.exodia.inventario.unit.aplicacion.comando;

import com.exodia.inventario.aplicacion.comando.BarcodeService;
import com.exodia.inventario.aplicacion.comando.OperacionService;
import com.exodia.inventario.aplicacion.comando.impl.AjusteInventarioServiceImpl;
import com.exodia.inventario.aplicacion.consulta.StockQueryService;
import com.exodia.inventario.domain.enums.TipoOperacionCodigo;
import com.exodia.inventario.domain.modelo.ajuste.Ajuste;
import com.exodia.inventario.domain.modelo.catalogo.*;
import com.exodia.inventario.domain.modelo.contenedor.Contenedor;
import com.exodia.inventario.domain.modelo.contenedor.Operacion;
import com.exodia.inventario.domain.politica.PoliticaDeduccionStock;
import com.exodia.inventario.excepcion.EntidadNoEncontradaException;
import com.exodia.inventario.excepcion.StockInsuficienteException;
import com.exodia.inventario.interfaz.dto.peticion.AjusteLineaRequest;
import com.exodia.inventario.interfaz.dto.peticion.CrearAjusteRequest;
import com.exodia.inventario.interfaz.dto.respuesta.AjusteResponse;
import com.exodia.inventario.interfaz.mapeador.AjusteMapeador;
import com.exodia.inventario.repositorio.ajuste.AjusteRepository;
import com.exodia.inventario.repositorio.catalogo.BodegaRepository;
import com.exodia.inventario.repositorio.catalogo.EmpresaRepository;
import com.exodia.inventario.repositorio.catalogo.TipoAjusteRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class AjusteInventarioServiceTest {

    @Mock private AjusteRepository ajusteRepository;
    @Mock private EmpresaRepository empresaRepository;
    @Mock private BodegaRepository bodegaRepository;
    @Mock private TipoAjusteRepository tipoAjusteRepository;
    @Mock private ContenedorRepository contenedorRepository;
    @Mock private OperacionService operacionService;
    @Mock private StockQueryService stockQueryService;
    @Mock private BarcodeService barcodeService;
    @Mock private AjusteMapeador ajusteMapeador;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private PoliticaDeduccionStock politicaDeduccionStock;

    @InjectMocks
    private AjusteInventarioServiceImpl ajusteService;

    private Empresa empresa;
    private Bodega bodega;
    private Contenedor contenedor;
    private TipoAjuste tipoAjuste;

    @BeforeEach
    void setUp() {
        empresa = Empresa.builder().codigo("EMP1").nombre("Test").build();
        empresa.setId(1L);

        bodega = Bodega.builder().empresa(empresa).codigo("BOD1").nombre("Bodega").build();
        bodega.setId(1L);
        bodega.setActivo(true);

        EstadoContenedor estado = EstadoContenedor.builder().codigo("DISPONIBLE").build();
        estado.setId(1L);

        contenedor = Contenedor.builder()
                .empresa(empresa).codigoBarras("INV001").productoId(100L)
                .bodega(bodega).estado(estado).precioUnitario(new BigDecimal("50.00"))
                .build();
        contenedor.setId(1L);

        tipoAjuste = TipoAjuste.builder().codigo("INVENTARIO").nombre("Ajuste Inventario").build();
        tipoAjuste.setId(1L);
    }

    @Test
    void deberiaCrearAjustePositivo() {
        AjusteLineaRequest lineaReq = new AjusteLineaRequest(1L, new BigDecimal("15"), null);
        CrearAjusteRequest request = new CrearAjusteRequest(1L, "INVENTARIO", "Ajuste test", List.of(lineaReq));

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(bodegaRepository.findById(1L)).thenReturn(Optional.of(bodega));
        when(tipoAjusteRepository.findByCodigo("INVENTARIO")).thenReturn(Optional.of(tipoAjuste));
        when(barcodeService.generarBarcode(1L, "AJU")).thenReturn("AJU00000001");
        when(ajusteRepository.save(any(Ajuste.class))).thenAnswer(inv -> {
            Ajuste a = inv.getArgument(0);
            a.setId(1L);
            return a;
        });
        when(stockQueryService.obtenerStockContenedor(1L)).thenReturn(new BigDecimal("10"));
        when(contenedorRepository.findById(1L)).thenReturn(Optional.of(contenedor));
        when(operacionService.crearOperacion(any(), eq(TipoOperacionCodigo.AJUSTE_POSITIVO),
                any(), any(), any(), any(), any()))
                .thenReturn(Operacion.builder().build());
        when(ajusteMapeador.toResponse(any(Ajuste.class)))
                .thenReturn(new AjusteResponse(1L, "AJU00000001", 1L, "INVENTARIO",
                        "Ajuste Inventario", "Ajuste test", null, List.of(), null));

        AjusteResponse response = ajusteService.crear(1L, request);

        assertNotNull(response);
        verify(operacionService).crearOperacion(any(), eq(TipoOperacionCodigo.AJUSTE_POSITIVO),
                any(), any(), any(), any(), any());
    }

    @Test
    void deberiaCrearAjusteNegativoConLockPesimista() {
        AjusteLineaRequest lineaReq = new AjusteLineaRequest(1L, new BigDecimal("5"), null);
        CrearAjusteRequest request = new CrearAjusteRequest(1L, "INVENTARIO", "Ajuste test", List.of(lineaReq));

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(bodegaRepository.findById(1L)).thenReturn(Optional.of(bodega));
        when(tipoAjusteRepository.findByCodigo("INVENTARIO")).thenReturn(Optional.of(tipoAjuste));
        when(barcodeService.generarBarcode(1L, "AJU")).thenReturn("AJU00000001");
        when(ajusteRepository.save(any(Ajuste.class))).thenAnswer(inv -> {
            Ajuste a = inv.getArgument(0);
            a.setId(1L);
            return a;
        });
        when(stockQueryService.obtenerStockContenedor(1L)).thenReturn(new BigDecimal("10"));
        when(contenedorRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(contenedor));
        when(stockQueryService.obtenerStockDisponible(1L)).thenReturn(new BigDecimal("10"));
        when(politicaDeduccionStock.evaluar(any(), any(), any()))
                .thenReturn(new PoliticaDeduccionStock.ResultadoValidacion(true, null));
        when(operacionService.crearOperacion(any(), eq(TipoOperacionCodigo.AJUSTE_NEGATIVO),
                any(), any(), any(), any(), any()))
                .thenReturn(Operacion.builder().build());
        when(ajusteMapeador.toResponse(any(Ajuste.class)))
                .thenReturn(new AjusteResponse(1L, "AJU00000001", 1L, "INVENTARIO",
                        "Ajuste Inventario", "Ajuste test", null, List.of(), null));

        AjusteResponse response = ajusteService.crear(1L, request);

        assertNotNull(response);
        verify(contenedorRepository).findByIdForUpdate(1L);
    }

    @Test
    void deberiaFallarSiStockInsuficienteEnAjusteNegativo() {
        AjusteLineaRequest lineaReq = new AjusteLineaRequest(1L, new BigDecimal("5"), null);
        CrearAjusteRequest request = new CrearAjusteRequest(1L, "INVENTARIO", "Ajuste test", List.of(lineaReq));

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(bodegaRepository.findById(1L)).thenReturn(Optional.of(bodega));
        when(tipoAjusteRepository.findByCodigo("INVENTARIO")).thenReturn(Optional.of(tipoAjuste));
        when(barcodeService.generarBarcode(1L, "AJU")).thenReturn("AJU00000001");
        when(ajusteRepository.save(any(Ajuste.class))).thenAnswer(inv -> {
            Ajuste a = inv.getArgument(0);
            a.setId(1L);
            return a;
        });
        when(stockQueryService.obtenerStockContenedor(1L)).thenReturn(new BigDecimal("10"));
        when(contenedorRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(contenedor));
        when(stockQueryService.obtenerStockDisponible(1L)).thenReturn(new BigDecimal("3"));
        when(politicaDeduccionStock.evaluar(any(), any(), any()))
                .thenReturn(new PoliticaDeduccionStock.ResultadoValidacion(false, "Stock insuficiente"));

        assertThrows(StockInsuficienteException.class,
                () -> ajusteService.crear(1L, request));
    }

    @Test
    void deberiaFallarSiEmpresaNoExiste() {
        when(empresaRepository.findById(999L)).thenReturn(Optional.empty());

        CrearAjusteRequest request = new CrearAjusteRequest(1L, "INVENTARIO", "test", List.of());

        assertThrows(EntidadNoEncontradaException.class,
                () -> ajusteService.crear(999L, request));
    }
}

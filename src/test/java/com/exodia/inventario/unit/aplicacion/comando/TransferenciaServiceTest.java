package com.exodia.inventario.unit.aplicacion.comando;

import com.exodia.inventario.aplicacion.comando.BarcodeService;
import com.exodia.inventario.aplicacion.comando.OperacionService;
import com.exodia.inventario.aplicacion.comando.impl.TransferenciaServiceImpl;
import com.exodia.inventario.aplicacion.consulta.StockQueryService;
import com.exodia.inventario.domain.modelo.catalogo.*;
import com.exodia.inventario.domain.modelo.transferencia.Transferencia;
import com.exodia.inventario.domain.servicio.PoliticaFEFO;
import com.exodia.inventario.domain.servicio.ValidadorEstadoTransferencia;
import com.exodia.inventario.excepcion.EntidadNoEncontradaException;
import com.exodia.inventario.excepcion.OperacionInvalidaException;
import com.exodia.inventario.interfaz.dto.peticion.CrearTransferenciaRequest;
import com.exodia.inventario.interfaz.dto.peticion.TransferenciaLineaRequest;
import com.exodia.inventario.interfaz.dto.respuesta.TransferenciaResponse;
import com.exodia.inventario.interfaz.mapeador.TransferenciaMapeador;
import com.exodia.inventario.repositorio.catalogo.*;
import com.exodia.inventario.repositorio.contenedor.ContenedorRepository;
import com.exodia.inventario.repositorio.transferencia.TransferenciaRepository;
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
class TransferenciaServiceTest {

    @Mock private TransferenciaRepository transferenciaRepository;
    @Mock private EmpresaRepository empresaRepository;
    @Mock private BodegaRepository bodegaRepository;
    @Mock private UnidadRepository unidadRepository;
    @Mock private UbicacionRepository ubicacionRepository;
    @Mock private EstadoTransferenciaRepository estadoTransferenciaRepository;
    @Mock private ContenedorRepository contenedorRepository;
    @Mock private OperacionService operacionService;
    @Mock private StockQueryService stockQueryService;
    @Mock private BarcodeService barcodeService;
    @Mock private PoliticaFEFO politicaFEFO;
    @Mock private ValidadorEstadoTransferencia validadorEstadoTransferencia;
    @Mock private TransferenciaMapeador transferenciaMapeador;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TransferenciaServiceImpl transferenciaService;

    private Empresa empresa;
    private Bodega bodegaOrigen;
    private Bodega bodegaDestino;
    private EstadoTransferencia estadoBorrador;

    @BeforeEach
    void setUp() {
        empresa = Empresa.builder().codigo("EMP1").nombre("Test").build();
        empresa.setId(1L);

        bodegaOrigen = Bodega.builder().empresa(empresa).codigo("BOD-O").nombre("Origen").build();
        bodegaOrigen.setId(1L);
        bodegaOrigen.setActivo(true);

        bodegaDestino = Bodega.builder().empresa(empresa).codigo("BOD-D").nombre("Destino").build();
        bodegaDestino.setId(2L);
        bodegaDestino.setActivo(true);

        estadoBorrador = EstadoTransferencia.builder().codigo("BORRADOR").nombre("Borrador").build();
        estadoBorrador.setId(1L);
    }

    @Test
    void deberiaCrearTransferenciaEnBorrador() {
        TransferenciaLineaRequest lineaReq = new TransferenciaLineaRequest(100L, 1L, new BigDecimal("5"), null);
        CrearTransferenciaRequest request = new CrearTransferenciaRequest(
                1L, 2L, "POR_PRODUCTO", null, List.of(lineaReq));

        Unidad unidad = Unidad.builder().empresa(empresa).nombre("UND").abreviatura("UND").build();
        unidad.setId(1L);
        unidad.setActivo(true);

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(bodegaRepository.findById(1L)).thenReturn(Optional.of(bodegaOrigen));
        when(bodegaRepository.findById(2L)).thenReturn(Optional.of(bodegaDestino));
        when(estadoTransferenciaRepository.findByCodigo("BORRADOR")).thenReturn(Optional.of(estadoBorrador));
        when(barcodeService.generarBarcode(1L, "TRF")).thenReturn("TRF00000001");
        when(unidadRepository.findById(1L)).thenReturn(Optional.of(unidad));
        when(transferenciaRepository.save(any(Transferencia.class))).thenAnswer(inv -> {
            Transferencia t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });
        when(transferenciaMapeador.toResponse(any(Transferencia.class)))
                .thenReturn(new TransferenciaResponse(1L, "TRF00000001", "POR_PRODUCTO",
                        1L, "BOD-O", 2L, "BOD-D", "BORRADOR", null,
                        null, null, List.of(), List.of(), null));

        TransferenciaResponse response = transferenciaService.crear(1L, request);

        assertNotNull(response);
        assertEquals("BORRADOR", response.estadoCodigo());
    }

    @Test
    void deberiaFallarSiBodegaOrigenIgualADestino() {
        CrearTransferenciaRequest request = new CrearTransferenciaRequest(
                1L, 1L, "POR_PRODUCTO", null, List.of());

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(bodegaRepository.findById(1L)).thenReturn(Optional.of(bodegaOrigen));

        assertThrows(OperacionInvalidaException.class,
                () -> transferenciaService.crear(1L, request));
    }

    @Test
    void deberiaFallarSiEmpresaNoExiste() {
        when(empresaRepository.findById(999L)).thenReturn(Optional.empty());

        CrearTransferenciaRequest request = new CrearTransferenciaRequest(
                1L, 2L, "POR_PRODUCTO", null, List.of());

        assertThrows(EntidadNoEncontradaException.class,
                () -> transferenciaService.crear(999L, request));
    }
}

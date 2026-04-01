package com.exodia.inventario.unit.aplicacion.comando;

import com.exodia.inventario.aplicacion.comando.impl.OperacionServiceImpl;
import com.exodia.inventario.domain.enums.TipoOperacionCodigo;
import com.exodia.inventario.domain.enums.TipoReferencia;
import com.exodia.inventario.domain.modelo.catalogo.*;
import com.exodia.inventario.domain.modelo.contenedor.Contenedor;
import com.exodia.inventario.domain.modelo.contenedor.Operacion;
import com.exodia.inventario.domain.servicio.CalculadorStock;
import com.exodia.inventario.excepcion.OperacionInvalidaException;
import com.exodia.inventario.repositorio.catalogo.TipoOperacionRepository;
import com.exodia.inventario.repositorio.contenedor.OperacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class OperacionServiceTest {

    @Mock
    private OperacionRepository operacionRepository;

    @Mock
    private TipoOperacionRepository tipoOperacionRepository;

    @Mock
    private CalculadorStock calculadorStock;

    @InjectMocks
    private OperacionServiceImpl operacionService;

    private Contenedor contenedor;
    private TipoOperacion tipoOperacionRecepcion;

    @BeforeEach
    void setUp() {
        Empresa empresa = Empresa.builder().build();
        empresa.setId(1L);

        Bodega bodega = Bodega.builder().build();
        bodega.setId(1L);

        Ubicacion ubicacion = Ubicacion.builder().build();
        ubicacion.setId(1L);

        Unidad unidad = Unidad.builder().build();
        unidad.setId(1L);

        contenedor = Contenedor.builder()
                .empresa(empresa)
                .codigoBarras("INV00000001")
                .productoId(100L)
                .bodega(bodega)
                .ubicacion(ubicacion)
                .unidad(unidad)
                .precioUnitario(new BigDecimal("50.00"))
                .numeroLote("LOTE-001")
                .proveedorId(10L)
                .build();
        contenedor.setId(1L);

        tipoOperacionRecepcion = TipoOperacion.builder()
                .codigo("RECEPCION")
                .nombre("Recepcion")
                .signo((short) 1)
                .build();
        tipoOperacionRecepcion.setId(1L);
    }

    @Test
    void deberiaCrearOperacionDeRecepcion() {
        when(tipoOperacionRepository.findByCodigo("RECEPCION"))
                .thenReturn(Optional.of(tipoOperacionRecepcion));
        when(calculadorStock.aplicarSigno(new BigDecimal("10"), 1))
                .thenReturn(new BigDecimal("10"));
        when(operacionRepository.save(any(Operacion.class)))
                .thenAnswer(invocation -> {
                    Operacion op = invocation.getArgument(0);
                    op.setId(1L);
                    return op;
                });

        Operacion resultado = operacionService.crearOperacion(
                contenedor,
                TipoOperacionCodigo.RECEPCION,
                new BigDecimal("10"),
                "Recepcion de mercaderia",
                TipoReferencia.RECEPCION,
                100L,
                200L);

        assertNotNull(resultado);
        assertEquals(contenedor, resultado.getContenedor());
        assertEquals("INV00000001", resultado.getCodigoBarras());
        assertEquals(100L, resultado.getProductoId());
        assertEquals(new BigDecimal("10"), resultado.getCantidad());
        assertEquals(TipoReferencia.RECEPCION, resultado.getTipoReferencia());
        assertEquals(100L, resultado.getReferenciaId());
        assertEquals(200L, resultado.getReferenciaLineaId());

        verify(operacionRepository).save(any(Operacion.class));
    }

    @Test
    void deberiaCrearOperacionSinReferencia() {
        when(tipoOperacionRepository.findByCodigo("RECEPCION"))
                .thenReturn(Optional.of(tipoOperacionRecepcion));
        when(calculadorStock.aplicarSigno(new BigDecimal("5"), 1))
                .thenReturn(new BigDecimal("5"));
        when(operacionRepository.save(any(Operacion.class)))
                .thenAnswer(invocation -> {
                    Operacion op = invocation.getArgument(0);
                    op.setId(2L);
                    return op;
                });

        Operacion resultado = operacionService.crearOperacion(
                contenedor,
                TipoOperacionCodigo.RECEPCION,
                new BigDecimal("5"),
                "Sin referencia");

        assertNotNull(resultado);
        assertNull(resultado.getTipoReferencia());
        assertNull(resultado.getReferenciaId());
    }

    @Test
    void deberiaAplicarSignoNegativoParaPicking() {
        TipoOperacion tipoPicking = TipoOperacion.builder()
                .codigo("PICKING")
                .nombre("Picking")
                .signo((short) -1)
                .build();
        tipoPicking.setId(2L);

        when(tipoOperacionRepository.findByCodigo("PICKING"))
                .thenReturn(Optional.of(tipoPicking));
        when(calculadorStock.aplicarSigno(new BigDecimal("3"), -1))
                .thenReturn(new BigDecimal("-3"));
        when(operacionRepository.save(any(Operacion.class)))
                .thenAnswer(invocation -> {
                    Operacion op = invocation.getArgument(0);
                    op.setId(3L);
                    return op;
                });

        Operacion resultado = operacionService.crearOperacion(
                contenedor,
                TipoOperacionCodigo.PICKING,
                new BigDecimal("3"),
                "Picking de venta");

        ArgumentCaptor<Operacion> captor = ArgumentCaptor.forClass(Operacion.class);
        verify(operacionRepository).save(captor.capture());
        assertEquals(0, new BigDecimal("-3").compareTo(captor.getValue().getCantidad()));
    }

    @Test
    void deberiaFallarConContenedorNulo() {
        assertThrows(OperacionInvalidaException.class,
                () -> operacionService.crearOperacion(
                        null,
                        TipoOperacionCodigo.RECEPCION,
                        new BigDecimal("10"),
                        "test"));
    }

    @Test
    void deberiaFallarConTipoOperacionNulo() {
        assertThrows(OperacionInvalidaException.class,
                () -> operacionService.crearOperacion(
                        contenedor,
                        null,
                        new BigDecimal("10"),
                        "test"));
    }

    @Test
    void deberiaFallarConCantidadCero() {
        assertThrows(OperacionInvalidaException.class,
                () -> operacionService.crearOperacion(
                        contenedor,
                        TipoOperacionCodigo.RECEPCION,
                        BigDecimal.ZERO,
                        "test"));
    }

    @Test
    void deberiaFallarConCantidadNegativa() {
        assertThrows(OperacionInvalidaException.class,
                () -> operacionService.crearOperacion(
                        contenedor,
                        TipoOperacionCodigo.RECEPCION,
                        new BigDecimal("-5"),
                        "test"));
    }

    @Test
    void deberiaFallarConCantidadNula() {
        assertThrows(OperacionInvalidaException.class,
                () -> operacionService.crearOperacion(
                        contenedor,
                        TipoOperacionCodigo.RECEPCION,
                        null,
                        "test"));
    }

    @Test
    void deberiaFallarSiTipoOperacionNoExisteEnCatalogo() {
        when(tipoOperacionRepository.findByCodigo("RECEPCION"))
                .thenReturn(Optional.empty());

        assertThrows(OperacionInvalidaException.class,
                () -> operacionService.crearOperacion(
                        contenedor,
                        TipoOperacionCodigo.RECEPCION,
                        new BigDecimal("10"),
                        "test"));
    }

    @Test
    void deberiaCopiarDatosDelContenedorALaOperacion() {
        when(tipoOperacionRepository.findByCodigo("RECEPCION"))
                .thenReturn(Optional.of(tipoOperacionRecepcion));
        when(calculadorStock.aplicarSigno(any(), anyInt()))
                .thenReturn(new BigDecimal("10"));
        when(operacionRepository.save(any(Operacion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        operacionService.crearOperacion(
                contenedor,
                TipoOperacionCodigo.RECEPCION,
                new BigDecimal("10"),
                "test");

        ArgumentCaptor<Operacion> captor = ArgumentCaptor.forClass(Operacion.class);
        verify(operacionRepository).save(captor.capture());

        Operacion guardada = captor.getValue();
        assertEquals(contenedor.getEmpresa(), guardada.getEmpresa());
        assertEquals(contenedor.getCodigoBarras(), guardada.getCodigoBarras());
        assertEquals(contenedor.getProductoId(), guardada.getProductoId());
        assertEquals(contenedor.getBodega(), guardada.getBodega());
        assertEquals(contenedor.getUbicacion(), guardada.getUbicacion());
        assertEquals(contenedor.getUnidad(), guardada.getUnidad());
        assertEquals(contenedor.getPrecioUnitario(), guardada.getPrecioUnitario());
        assertEquals(contenedor.getNumeroLote(), guardada.getNumeroLote());
        assertEquals(contenedor.getProveedorId(), guardada.getProveedorId());
    }
}

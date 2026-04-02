package com.exodia.inventario.unit.aplicacion.comando;

import com.exodia.inventario.aplicacion.comando.OperacionService;
import com.exodia.inventario.aplicacion.comando.impl.MovimientoContenedorServiceImpl;
import com.exodia.inventario.aplicacion.consulta.StockQueryService;
import com.exodia.inventario.domain.enums.EstadoContenedorCodigo;
import com.exodia.inventario.domain.enums.TipoUbicacion;
import com.exodia.inventario.domain.modelo.catalogo.Bodega;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;
import com.exodia.inventario.domain.modelo.catalogo.EstadoContenedor;
import com.exodia.inventario.domain.modelo.catalogo.Ubicacion;
import com.exodia.inventario.domain.modelo.contenedor.Contenedor;
import com.exodia.inventario.domain.modelo.contenedor.Operacion;
import com.exodia.inventario.excepcion.OperacionInvalidaException;
import com.exodia.inventario.interfaz.dto.peticion.MoverContenedorRequest;
import com.exodia.inventario.interfaz.dto.peticion.OperacionContenedorRequest;
import com.exodia.inventario.interfaz.dto.respuesta.MovimientoContenedorResponse;
import com.exodia.inventario.repositorio.catalogo.EstadoContenedorRepository;
import com.exodia.inventario.repositorio.catalogo.UbicacionRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class MovimientoContenedorServiceTest {

    @Mock private ContenedorRepository contenedorRepository;
    @Mock private UbicacionRepository ubicacionRepository;
    @Mock private EstadoContenedorRepository estadoContenedorRepository;
    @Mock private StockQueryService stockQueryService;
    @Mock private OperacionService operacionService;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private MovimientoContenedorServiceImpl movimientoContenedorService;

    private Empresa empresa;
    private Bodega bodega;
    private Ubicacion ubicacionOrigen;
    private Ubicacion ubicacionDestino;
    private Ubicacion ubicacionStandby;
    private EstadoContenedor estadoDisponible;
    private EstadoContenedor estadoStandby;
    private EstadoContenedor estadoCuarentena;

    @BeforeEach
    void setUp() {
        empresa = Empresa.builder().codigo("EMP1").nombre("Empresa Test").build();
        empresa.setId(1L);

        bodega = Bodega.builder().empresa(empresa).codigo("BOD-1").nombre("Central").build();
        bodega.setId(1L);
        bodega.setActivo(true);

        ubicacionOrigen = crearUbicacion(10L, "A-01", TipoUbicacion.GENERAL);
        ubicacionDestino = crearUbicacion(11L, "A-02", TipoUbicacion.GENERAL);
        ubicacionStandby = crearUbicacion(12L, "STD-01", TipoUbicacion.STANDBY);
        bodega.setUbicacionStandby(ubicacionStandby);

        estadoDisponible = crearEstado(1L, EstadoContenedorCodigo.DISPONIBLE.getCodigo());
        estadoStandby = crearEstado(2L, EstadoContenedorCodigo.EN_STANDBY.getCodigo());
        estadoCuarentena = crearEstado(3L, EstadoContenedorCodigo.CUARENTENA.getCodigo());
    }

    @Test
    void deberiaPreservarEstadoNoOperativoEnMovimientoNormal() {
        Contenedor contenedor = crearContenedor(100L, estadoCuarentena, ubicacionOrigen);

        when(contenedorRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(contenedor));
        when(ubicacionRepository.findById(ubicacionDestino.getId())).thenReturn(Optional.of(ubicacionDestino));
        when(stockQueryService.obtenerStockContenedor(100L)).thenReturn(new BigDecimal("8"));
        when(stockQueryService.obtenerCantidadReservada(100L)).thenReturn(BigDecimal.ZERO);
        when(contenedorRepository.save(any(Contenedor.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(operacionService.crearOperacion(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(crearOperacion(1L), crearOperacion(2L));

        MovimientoContenedorResponse response = movimientoContenedorService.mover(
                1L, 100L, new MoverContenedorRequest(ubicacionDestino.getId(), "Reubicar"));

        assertEquals(EstadoContenedorCodigo.CUARENTENA.getCodigo(), response.estadoAnterior());
        assertEquals(EstadoContenedorCodigo.CUARENTENA.getCodigo(), response.estadoNuevo());
        assertEquals(EstadoContenedorCodigo.CUARENTENA.getCodigo(), contenedor.getEstado().getCodigo());
    }

    @Test
    void deberiaFallarSiContenedorTieneReservasActivas() {
        Contenedor contenedor = crearContenedor(100L, estadoDisponible, ubicacionOrigen);

        when(contenedorRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(contenedor));
        when(ubicacionRepository.findById(ubicacionDestino.getId())).thenReturn(Optional.of(ubicacionDestino));
        when(stockQueryService.obtenerStockContenedor(100L)).thenReturn(new BigDecimal("8"));
        when(stockQueryService.obtenerCantidadReservada(100L)).thenReturn(new BigDecimal("2"));

        OperacionInvalidaException exception = assertThrows(OperacionInvalidaException.class,
                () -> movimientoContenedorService.mover(
                        1L, 100L, new MoverContenedorRequest(ubicacionDestino.getId(), null)));

        assertTrue(exception.getMessage().contains("reservadas activas"));
        verify(operacionService, never()).crearOperacion(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void deberiaFallarSiSeIntentaEnviarAStandbyUnContenedorNoDisponible() {
        Contenedor contenedor = crearContenedor(100L, estadoCuarentena, ubicacionOrigen);

        when(contenedorRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(contenedor));

        OperacionInvalidaException exception = assertThrows(OperacionInvalidaException.class,
                () -> movimientoContenedorService.enviarAStandby(
                        1L, 100L, new OperacionContenedorRequest("standby")));

        assertTrue(exception.getMessage().contains("estado DISPONIBLE"));
        verify(stockQueryService, never()).obtenerStockContenedor(any());
    }

    @Test
    void deberiaMarcarDisponibleAlSalirDeStandby() {
        Contenedor contenedor = crearContenedor(100L, estadoStandby, ubicacionStandby);

        when(contenedorRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(contenedor));
        when(ubicacionRepository.findById(ubicacionDestino.getId())).thenReturn(Optional.of(ubicacionDestino));
        when(stockQueryService.obtenerStockContenedor(100L)).thenReturn(new BigDecimal("5"));
        when(stockQueryService.obtenerCantidadReservada(100L)).thenReturn(BigDecimal.ZERO);
        when(estadoContenedorRepository.findByCodigo(EstadoContenedorCodigo.DISPONIBLE.getCodigo()))
                .thenReturn(Optional.of(estadoDisponible));
        when(contenedorRepository.save(any(Contenedor.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(operacionService.crearOperacion(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(crearOperacion(1L), crearOperacion(2L));

        MovimientoContenedorResponse response = movimientoContenedorService.sacarDeStandby(
                1L, 100L, new MoverContenedorRequest(ubicacionDestino.getId(), "salir"));

        assertEquals(EstadoContenedorCodigo.EN_STANDBY.getCodigo(), response.estadoAnterior());
        assertEquals(EstadoContenedorCodigo.DISPONIBLE.getCodigo(), response.estadoNuevo());
        assertEquals(EstadoContenedorCodigo.DISPONIBLE.getCodigo(), contenedor.getEstado().getCodigo());
    }

    private Ubicacion crearUbicacion(Long id, String codigo, TipoUbicacion tipoUbicacion) {
        Ubicacion ubicacion = Ubicacion.builder()
                .bodega(bodega)
                .codigo(codigo)
                .nombre(codigo)
                .tipoUbicacion(tipoUbicacion)
                .build();
        ubicacion.setId(id);
        ubicacion.setActivo(true);
        return ubicacion;
    }

    private EstadoContenedor crearEstado(Long id, String codigo) {
        EstadoContenedor estado = EstadoContenedor.builder().codigo(codigo).nombre(codigo).build();
        estado.setId(id);
        return estado;
    }

    private Contenedor crearContenedor(Long id, EstadoContenedor estado, Ubicacion ubicacion) {
        Contenedor contenedor = Contenedor.builder()
                .empresa(empresa)
                .codigoBarras("INV-" + id)
                .productoId(200L)
                .bodega(bodega)
                .ubicacion(ubicacion)
                .estado(estado)
                .build();
        contenedor.setId(id);
        contenedor.setActivo(true);
        return contenedor;
    }

    private Operacion crearOperacion(Long id) {
        Operacion operacion = Operacion.builder().build();
        operacion.setId(id);
        return operacion;
    }
}

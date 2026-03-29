package com.exodia.inventario.unit.aplicacion.comando;

import com.exodia.inventario.aplicacion.comando.impl.ReservaServiceImpl;
import com.exodia.inventario.aplicacion.consulta.StockQueryService;
import com.exodia.inventario.domain.enums.TipoReferencia;
import com.exodia.inventario.domain.modelo.catalogo.Bodega;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;
import com.exodia.inventario.domain.modelo.catalogo.EstadoContenedor;
import com.exodia.inventario.domain.modelo.contenedor.Contenedor;
import com.exodia.inventario.domain.modelo.contenedor.Reserva;
import com.exodia.inventario.domain.politica.PoliticaReserva;
import com.exodia.inventario.excepcion.EntidadNoEncontradaException;
import com.exodia.inventario.excepcion.OperacionInvalidaException;
import com.exodia.inventario.interfaz.dto.peticion.CrearReservaRequest;
import com.exodia.inventario.interfaz.dto.respuesta.ReservaResponse;
import com.exodia.inventario.interfaz.mapeador.ReservaMapeador;
import com.exodia.inventario.repositorio.contenedor.ContenedorRepository;
import com.exodia.inventario.repositorio.contenedor.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class ReservaServiceTest {

    @Mock private ReservaRepository reservaRepository;
    @Mock private ContenedorRepository contenedorRepository;
    @Mock private StockQueryService stockQueryService;
    @Mock private PoliticaReserva politicaReserva;
    @Mock private ReservaMapeador reservaMapeador;

    @InjectMocks
    private ReservaServiceImpl reservaService;

    private Empresa empresa;
    private Bodega bodega;
    private Contenedor contenedor;

    @BeforeEach
    void setUp() {
        empresa = Empresa.builder().codigo("EMP1").nombre("Test").build();
        empresa.setId(1L);

        bodega = Bodega.builder().empresa(empresa).codigo("BOD1").nombre("Bodega").build();
        bodega.setId(1L);

        EstadoContenedor estado = EstadoContenedor.builder().codigo("DISPONIBLE").build();
        estado.setId(1L);

        contenedor = Contenedor.builder()
                .empresa(empresa).codigoBarras("INV001").productoId(100L)
                .bodega(bodega).estado(estado).build();
        contenedor.setId(1L);
    }

    @Test
    void deberiaCrearReserva() {
        CrearReservaRequest request = new CrearReservaRequest(
                1L, new BigDecimal("5"), TipoReferencia.VENTA, 100L, null, null);

        when(contenedorRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(contenedor));
        when(stockQueryService.obtenerStockDisponible(1L)).thenReturn(new BigDecimal("10"));
        when(politicaReserva.evaluar(any(), any(), any()))
                .thenReturn(new PoliticaReserva.ResultadoValidacion(true, null));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(inv -> {
            Reserva r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });
        when(reservaMapeador.toResponse(any(Reserva.class)))
                .thenReturn(new ReservaResponse(1L, 1L, "INV001", 100L, 1L,
                        new BigDecimal("5"), BigDecimal.ZERO, new BigDecimal("5"),
                        "PENDIENTE", "VENTA", 100L, null, null));

        ReservaResponse response = reservaService.crear(1L, request);

        assertNotNull(response);
        assertEquals("PENDIENTE", response.estado());
        verify(contenedorRepository).findByIdForUpdate(1L);
    }

    @Test
    void deberiaFallarSiStockInsuficiente() {
        CrearReservaRequest request = new CrearReservaRequest(
                1L, new BigDecimal("100"), TipoReferencia.VENTA, 100L, null, null);

        when(contenedorRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(contenedor));
        when(stockQueryService.obtenerStockDisponible(1L)).thenReturn(new BigDecimal("5"));
        when(politicaReserva.evaluar(any(), any(), any()))
                .thenReturn(new PoliticaReserva.ResultadoValidacion(false, "Stock insuficiente"));

        assertThrows(OperacionInvalidaException.class,
                () -> reservaService.crear(1L, request));
    }

    @Test
    void deberiaFallarAlCancelarReservaYaCancelada() {
        Reserva reserva = Reserva.builder()
                .empresa(empresa).contenedor(contenedor).estado("CANCELADA").build();
        reserva.setId(1L);

        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        assertThrows(OperacionInvalidaException.class,
                () -> reservaService.cancelar(1L, 1L));
    }

    @Test
    void deberiaFallarSiContenedorNoExiste() {
        CrearReservaRequest request = new CrearReservaRequest(
                999L, new BigDecimal("5"), TipoReferencia.VENTA, 100L, null, null);

        when(contenedorRepository.findByIdForUpdate(999L)).thenReturn(Optional.empty());

        assertThrows(EntidadNoEncontradaException.class,
                () -> reservaService.crear(1L, request));
    }
}

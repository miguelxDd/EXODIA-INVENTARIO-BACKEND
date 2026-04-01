package com.exodia.inventario.unit.aplicacion.comando;

import com.exodia.inventario.aplicacion.comando.OperacionService;
import com.exodia.inventario.aplicacion.comando.impl.MermaServiceImpl;
import com.exodia.inventario.aplicacion.consulta.StockQueryService;
import com.exodia.inventario.domain.enums.TipoOperacionCodigo;
import com.exodia.inventario.domain.modelo.catalogo.Bodega;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;
import com.exodia.inventario.domain.modelo.catalogo.EstadoContenedor;
import com.exodia.inventario.domain.modelo.contenedor.Contenedor;
import com.exodia.inventario.domain.modelo.contenedor.Operacion;
import com.exodia.inventario.domain.modelo.extension.RegistroMerma;
import com.exodia.inventario.domain.politica.PoliticaDeduccionStock;
import com.exodia.inventario.excepcion.StockInsuficienteException;
import com.exodia.inventario.interfaz.dto.peticion.CrearMermaRequest;
import com.exodia.inventario.interfaz.dto.respuesta.MermaResponse;
import com.exodia.inventario.interfaz.mapeador.MermaMapeador;
import com.exodia.inventario.repositorio.contenedor.ContenedorRepository;
import com.exodia.inventario.repositorio.extension.ConfigMermaRepository;
import com.exodia.inventario.repositorio.extension.ConfiguracionProductoRepository;
import com.exodia.inventario.repositorio.extension.RegistroMermaRepository;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class MermaServiceTest {

    @Mock private RegistroMermaRepository registroMermaRepository;
    @Mock private ConfigMermaRepository configMermaRepository;
    @Mock private ConfiguracionProductoRepository configuracionProductoRepository;
    @Mock private ContenedorRepository contenedorRepository;
    @Mock private OperacionService operacionService;
    @Mock private StockQueryService stockQueryService;
    @Mock private PoliticaDeduccionStock politicaDeduccionStock;
    @Mock private MermaMapeador mermaMapeador;

    @InjectMocks
    private MermaServiceImpl mermaService;

    private Contenedor contenedor;

    @BeforeEach
    void setUp() {
        Empresa empresa = Empresa.builder().codigo("EMP1").nombre("Test").build();
        empresa.setId(1L);

        Bodega bodega = Bodega.builder().empresa(empresa).codigo("BOD1").build();
        bodega.setId(1L);

        EstadoContenedor estado = EstadoContenedor.builder().codigo("DISPONIBLE").build();
        estado.setId(1L);

        contenedor = Contenedor.builder()
                .empresa(empresa).codigoBarras("INV001").productoId(100L)
                .bodega(bodega).estado(estado).build();
        contenedor.setId(1L);
    }

    @Test
    void deberiaRegistrarMerma() {
        CrearMermaRequest request = new CrearMermaRequest(1L, new BigDecimal("2"), "Merma test");

        when(contenedorRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(contenedor));
        when(stockQueryService.obtenerStockDisponible(1L)).thenReturn(new BigDecimal("10"));
        when(politicaDeduccionStock.evaluar(any(), any(), any()))
                .thenReturn(new PoliticaDeduccionStock.ResultadoValidacion(true, null));
        when(operacionService.crearOperacion(any(), eq(TipoOperacionCodigo.MERMA), any(), any()))
                .thenReturn(Operacion.builder().build());
        when(registroMermaRepository.save(any(RegistroMerma.class))).thenAnswer(inv -> {
            RegistroMerma r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });
        when(mermaMapeador.toResponse(any(RegistroMerma.class)))
                .thenReturn(new MermaResponse(1L, 1L, new BigDecimal("2"),
                        "MANUAL", "Merma test", 1L, null));

        MermaResponse response = mermaService.registrar(1L, request);

        assertNotNull(response);
        verify(contenedorRepository).findByIdForUpdate(1L);
        verify(operacionService).crearOperacion(any(), eq(TipoOperacionCodigo.MERMA), any(), any());
    }

    @Test
    void deberiaFallarSiStockInsuficiente() {
        CrearMermaRequest request = new CrearMermaRequest(1L, new BigDecimal("100"), "Merma grande");

        when(contenedorRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(contenedor));
        when(stockQueryService.obtenerStockDisponible(1L)).thenReturn(new BigDecimal("5"));
        when(politicaDeduccionStock.evaluar(any(), any(), any()))
                .thenReturn(new PoliticaDeduccionStock.ResultadoValidacion(false, "Stock insuficiente"));

        assertThrows(StockInsuficienteException.class,
                () -> mermaService.registrar(1L, request));
    }
}

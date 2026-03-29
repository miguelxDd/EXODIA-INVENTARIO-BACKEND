package com.exodia.inventario.unit.aplicacion.comando;

import com.exodia.inventario.aplicacion.comando.BarcodeService;
import com.exodia.inventario.aplicacion.comando.OperacionService;
import com.exodia.inventario.aplicacion.comando.impl.ConteoFisicoServiceImpl;
import com.exodia.inventario.aplicacion.consulta.StockQueryService;
import com.exodia.inventario.domain.modelo.catalogo.*;
import com.exodia.inventario.domain.modelo.conteo.ConteoFisico;
import com.exodia.inventario.excepcion.EntidadNoEncontradaException;
import com.exodia.inventario.excepcion.OperacionInvalidaException;
import com.exodia.inventario.interfaz.dto.peticion.CrearConteoFisicoRequest;
import com.exodia.inventario.interfaz.dto.respuesta.ConteoFisicoResponse;
import com.exodia.inventario.interfaz.mapeador.ConteoFisicoMapeador;
import com.exodia.inventario.repositorio.ajuste.AjusteRepository;
import com.exodia.inventario.repositorio.catalogo.BodegaRepository;
import com.exodia.inventario.repositorio.catalogo.EmpresaRepository;
import com.exodia.inventario.repositorio.conteo.ConteoFisicoRepository;
import com.exodia.inventario.repositorio.contenedor.ContenedorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class ConteoFisicoServiceTest {

    @Mock private ConteoFisicoRepository conteoFisicoRepository;
    @Mock private EmpresaRepository empresaRepository;
    @Mock private BodegaRepository bodegaRepository;
    @Mock private ContenedorRepository contenedorRepository;
    @Mock private AjusteRepository ajusteRepository;
    @Mock private OperacionService operacionService;
    @Mock private StockQueryService stockQueryService;
    @Mock private BarcodeService barcodeService;
    @Mock private ConteoFisicoMapeador conteoFisicoMapeador;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ConteoFisicoServiceImpl conteoFisicoService;

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
    void deberiaCrearConteoFisico() {
        CrearConteoFisicoRequest request = new CrearConteoFisicoRequest(1L, "Conteo mensual");

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(bodegaRepository.findById(1L)).thenReturn(Optional.of(bodega));
        when(barcodeService.generarBarcode(1L, "CNT")).thenReturn("CNT00000001");
        when(conteoFisicoRepository.save(any(ConteoFisico.class))).thenAnswer(inv -> {
            ConteoFisico c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });
        when(conteoFisicoMapeador.toResponse(any(ConteoFisico.class)))
                .thenReturn(new ConteoFisicoResponse(1L, "CNT00000001", 1L,
                        "EN_PROGRESO", null, "Conteo mensual", null, List.of(), null));

        ConteoFisicoResponse response = conteoFisicoService.crear(1L, request);

        assertNotNull(response);
        assertEquals("EN_PROGRESO", response.estado());
    }

    @Test
    void deberiaFallarAlAplicarConteoSinLineas() {
        ConteoFisico conteo = ConteoFisico.builder()
                .empresa(empresa).bodega(bodega).estado("EN_PROGRESO")
                .numeroConteo("CNT001").build();
        conteo.setId(1L);

        when(conteoFisicoRepository.findById(1L)).thenReturn(Optional.of(conteo));

        assertThrows(OperacionInvalidaException.class,
                () -> conteoFisicoService.aplicar(1L, 1L));
    }

    @Test
    void deberiaFallarAlAplicarConteoNoEnProgreso() {
        ConteoFisico conteo = ConteoFisico.builder()
                .empresa(empresa).bodega(bodega).estado("APLICADO")
                .numeroConteo("CNT001").build();
        conteo.setId(1L);

        when(conteoFisicoRepository.findById(1L)).thenReturn(Optional.of(conteo));

        assertThrows(OperacionInvalidaException.class,
                () -> conteoFisicoService.aplicar(1L, 1L));
    }

    @Test
    void deberiaFallarAlCancelarConteoNoEnProgreso() {
        ConteoFisico conteo = ConteoFisico.builder()
                .empresa(empresa).bodega(bodega).estado("APLICADO")
                .numeroConteo("CNT001").build();
        conteo.setId(1L);

        when(conteoFisicoRepository.findById(1L)).thenReturn(Optional.of(conteo));

        assertThrows(OperacionInvalidaException.class,
                () -> conteoFisicoService.cancelar(1L, 1L));
    }

    @Test
    void deberiaFallarSiEmpresaNoExiste() {
        when(empresaRepository.findById(999L)).thenReturn(Optional.empty());

        CrearConteoFisicoRequest request = new CrearConteoFisicoRequest(1L, null);

        assertThrows(EntidadNoEncontradaException.class,
                () -> conteoFisicoService.crear(999L, request));
    }
}

package com.exodia.inventario.unit.aplicacion.comando;

import com.exodia.inventario.aplicacion.comando.ConfiguracionEmpresaService;
import com.exodia.inventario.aplicacion.comando.impl.BarcodeServiceImpl;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;
import com.exodia.inventario.domain.modelo.extension.ConfiguracionEmpresa;
import com.exodia.inventario.domain.modelo.extension.SecuenciaBarcode;
import com.exodia.inventario.repositorio.catalogo.EmpresaRepository;
import com.exodia.inventario.repositorio.contenedor.ContenedorRepository;
import com.exodia.inventario.repositorio.extension.SecuenciaBarcodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class BarcodeServiceTest {

    @Mock
    private SecuenciaBarcodeRepository secuenciaBarcodeRepository;

    @Mock
    private ContenedorRepository contenedorRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private ConfiguracionEmpresaService configuracionEmpresaService;

    @InjectMocks
    private BarcodeServiceImpl barcodeService;

    private Empresa empresa;
    private SecuenciaBarcode secuencia;
    private ConfiguracionEmpresa configuracionEmpresa;

    @BeforeEach
    void setUp() {
        empresa = Empresa.builder().build();
        empresa.setId(1L);

        configuracionEmpresa = ConfiguracionEmpresa.builder()
                .empresa(empresa)
                .barcodePrefijo("INV")
                .barcodeLongitudPadding(8)
                .build();

        secuencia = SecuenciaBarcode.builder()
                .empresa(empresa)
                .prefijo("INV")
                .ultimoValor(0L)
                .longitudPadding(8)
                .build();
        secuencia.setId(1L);
    }

    @Test
    void deberiaGenerarPrimerBarcode() {
        when(configuracionEmpresaService.obtenerEntidadOCrear(1L))
                .thenReturn(configuracionEmpresa);
        when(secuenciaBarcodeRepository.findByEmpresaIdAndPrefijoForUpdate(1L, "INV"))
                .thenReturn(Optional.of(secuencia));
        when(secuenciaBarcodeRepository.save(any(SecuenciaBarcode.class)))
                .thenReturn(secuencia);

        String barcode = barcodeService.generarBarcode(1L);

        assertEquals("INV00000001", barcode);
        assertEquals(1L, secuencia.getUltimoValor());
        verify(secuenciaBarcodeRepository).save(secuencia);
    }

    @Test
    void deberiaIncrementarSecuencia() {
        secuencia.setUltimoValor(42L);
        when(configuracionEmpresaService.obtenerEntidadOCrear(1L))
                .thenReturn(configuracionEmpresa);
        when(secuenciaBarcodeRepository.findByEmpresaIdAndPrefijoForUpdate(1L, "INV"))
                .thenReturn(Optional.of(secuencia));
        when(secuenciaBarcodeRepository.save(any(SecuenciaBarcode.class)))
                .thenReturn(secuencia);

        String barcode = barcodeService.generarBarcode(1L);

        assertEquals("INV00000043", barcode);
        assertEquals(43L, secuencia.getUltimoValor());
    }

    @Test
    void deberiaGenerarBarcodeConPrefijoCustom() {
        SecuenciaBarcode secuenciaRec = SecuenciaBarcode.builder()
                .empresa(empresa)
                .prefijo("REC")
                .ultimoValor(99L)
                .longitudPadding(8)
                .build();
        secuenciaRec.setId(2L);

        when(secuenciaBarcodeRepository.findByEmpresaIdAndPrefijoForUpdate(1L, "REC"))
                .thenReturn(Optional.of(secuenciaRec));
        when(secuenciaBarcodeRepository.save(any(SecuenciaBarcode.class)))
                .thenReturn(secuenciaRec);

        String barcode = barcodeService.generarBarcode(1L, "REC");

        assertEquals("REC00000100", barcode);
    }

    @Test
    void deberiaCrearSecuenciaSiNoExiste() {
        when(configuracionEmpresaService.obtenerEntidadOCrear(1L))
                .thenReturn(configuracionEmpresa);
        when(secuenciaBarcodeRepository.findByEmpresaIdAndPrefijoForUpdate(1L, "INV"))
                .thenReturn(Optional.empty());
        when(empresaRepository.findById(1L))
                .thenReturn(Optional.of(empresa));
        when(secuenciaBarcodeRepository.save(any(SecuenciaBarcode.class)))
                .thenAnswer(invocation -> {
                    SecuenciaBarcode s = invocation.getArgument(0);
                    s.setId(10L);
                    return s;
                });

        String barcode = barcodeService.generarBarcode(1L);

        assertEquals("INV00000001", barcode);
        verify(secuenciaBarcodeRepository, times(2)).save(any(SecuenciaBarcode.class));
    }

    @Test
    void deberiaFallarSiEmpresaNoExisteAlCrearSecuencia() {
        ConfiguracionEmpresa config999 = ConfiguracionEmpresa.builder()
                .barcodePrefijo("INV")
                .barcodeLongitudPadding(8)
                .build();
        when(configuracionEmpresaService.obtenerEntidadOCrear(999L))
                .thenReturn(config999);
        when(secuenciaBarcodeRepository.findByEmpresaIdAndPrefijoForUpdate(999L, "INV"))
                .thenReturn(Optional.empty());
        when(empresaRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> barcodeService.generarBarcode(999L));
    }

    @Test
    void deberiaVerificarExistenciaDeBarcode() {
        when(contenedorRepository.existsByEmpresaIdAndCodigoBarras(1L, "INV00000001"))
                .thenReturn(true);
        when(contenedorRepository.existsByEmpresaIdAndCodigoBarras(1L, "INV99999999"))
                .thenReturn(false);

        assertTrue(barcodeService.existeBarcode(1L, "INV00000001"));
        assertFalse(barcodeService.existeBarcode(1L, "INV99999999"));
    }
}

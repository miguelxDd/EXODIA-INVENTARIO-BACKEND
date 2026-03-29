package com.exodia.inventario.unit.aplicacion.comando;

import com.exodia.inventario.aplicacion.comando.impl.LoteServiceImpl;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;
import com.exodia.inventario.domain.modelo.contenedor.Lote;
import com.exodia.inventario.excepcion.EntidadNoEncontradaException;
import com.exodia.inventario.repositorio.catalogo.EmpresaRepository;
import com.exodia.inventario.repositorio.contenedor.LoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class LoteServiceTest {

    @Mock private LoteRepository loteRepository;
    @Mock private EmpresaRepository empresaRepository;

    @InjectMocks
    private LoteServiceImpl loteService;

    private Empresa empresa;
    private Lote loteExistente;

    @BeforeEach
    void setUp() {
        empresa = Empresa.builder().codigo("EMP1").nombre("Test").build();
        empresa.setId(1L);

        loteExistente = Lote.builder()
                .empresa(empresa)
                .numeroLote("LOT-001")
                .productoId(100L)
                .fechaVencimiento(LocalDate.of(2027, 6, 15))
                .proveedorId(50L)
                .build();
        loteExistente.setId(1L);
    }

    @Test
    void deberiaRetornarLoteExistenteSiYaExiste() {
        when(loteRepository.findByEmpresaIdAndNumeroLoteAndProductoId(1L, "LOT-001", 100L))
                .thenReturn(Optional.of(loteExistente));

        Lote resultado = loteService.buscarOCrear(1L, 100L, "LOT-001",
                LocalDate.of(2027, 6, 15), 50L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("LOT-001", resultado.getNumeroLote());
        verify(loteRepository, never()).save(any());
        verify(empresaRepository, never()).findById(any());
    }

    @Test
    void deberiaCrearLoteNuevoSiNoExiste() {
        when(loteRepository.findByEmpresaIdAndNumeroLoteAndProductoId(1L, "LOT-002", 100L))
                .thenReturn(Optional.empty());
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(loteRepository.save(any(Lote.class))).thenAnswer(inv -> {
            Lote l = inv.getArgument(0);
            l.setId(2L);
            return l;
        });

        Lote resultado = loteService.buscarOCrear(1L, 100L, "LOT-002",
                LocalDate.of(2027, 12, 31), 50L);

        assertNotNull(resultado);
        assertEquals(2L, resultado.getId());
        assertEquals("LOT-002", resultado.getNumeroLote());
        assertEquals(100L, resultado.getProductoId());
        verify(loteRepository).save(any(Lote.class));
    }

    @Test
    void deberiaFallarSiEmpresaNoExisteAlCrearLoteNuevo() {
        when(loteRepository.findByEmpresaIdAndNumeroLoteAndProductoId(eq(999L), any(), any()))
                .thenReturn(Optional.empty());
        when(empresaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntidadNoEncontradaException.class,
                () -> loteService.buscarOCrear(999L, 100L, "LOT-003",
                        LocalDate.of(2027, 6, 15), null));
    }

    @Test
    void deberiaCrearLoteSinProveedorNiFechaVencimiento() {
        when(loteRepository.findByEmpresaIdAndNumeroLoteAndProductoId(1L, "LOT-004", 200L))
                .thenReturn(Optional.empty());
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(loteRepository.save(any(Lote.class))).thenAnswer(inv -> {
            Lote l = inv.getArgument(0);
            l.setId(3L);
            return l;
        });

        Lote resultado = loteService.buscarOCrear(1L, 200L, "LOT-004", null, null);

        assertNotNull(resultado);
        assertNull(resultado.getFechaVencimiento());
        assertNull(resultado.getProveedorId());
        verify(loteRepository).save(any(Lote.class));
    }
}

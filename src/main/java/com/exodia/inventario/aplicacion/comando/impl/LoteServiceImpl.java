package com.exodia.inventario.aplicacion.comando.impl;

import com.exodia.inventario.aplicacion.comando.LoteService;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;
import com.exodia.inventario.domain.modelo.contenedor.Lote;
import com.exodia.inventario.excepcion.EntidadNoEncontradaException;
import com.exodia.inventario.repositorio.catalogo.EmpresaRepository;
import com.exodia.inventario.repositorio.contenedor.LoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoteServiceImpl implements LoteService {

    private final LoteRepository loteRepository;
    private final EmpresaRepository empresaRepository;

    @Override
    @Transactional
    public Lote buscarOCrear(Long empresaId, Long productoId, String numeroLote,
                             LocalDate fechaVencimiento, Long proveedorId) {
        return loteRepository.findByEmpresaIdAndNumeroLoteAndProductoId(empresaId, numeroLote, productoId)
                .orElseGet(() -> {
                    Empresa empresa = empresaRepository.findById(empresaId)
                            .orElseThrow(() -> new EntidadNoEncontradaException("Empresa", empresaId));

                    Lote nuevo = Lote.builder()
                            .empresa(empresa)
                            .numeroLote(numeroLote)
                            .productoId(productoId)
                            .fechaVencimiento(fechaVencimiento)
                            .proveedorId(proveedorId)
                            .build();

                    nuevo = loteRepository.save(nuevo);
                    log.info("Lote {} creado para producto {} empresa {}", numeroLote, productoId, empresaId);
                    return nuevo;
                });
    }
}

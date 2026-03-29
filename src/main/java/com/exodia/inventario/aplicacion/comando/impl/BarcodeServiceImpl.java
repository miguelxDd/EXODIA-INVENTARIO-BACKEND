package com.exodia.inventario.aplicacion.comando.impl;

import com.exodia.inventario.aplicacion.comando.BarcodeService;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;
import com.exodia.inventario.domain.modelo.extension.SecuenciaBarcode;
import com.exodia.inventario.excepcion.BarcodeDuplicadoException;
import com.exodia.inventario.repositorio.catalogo.EmpresaRepository;
import com.exodia.inventario.repositorio.contenedor.ContenedorRepository;
import com.exodia.inventario.repositorio.extension.SecuenciaBarcodeRepository;
import com.exodia.inventario.util.InventarioConstantes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementacion del servicio de generacion de codigos de barras.
 * Usa lock pesimista sobre la secuencia para garantizar unicidad
 * en escenarios concurrentes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BarcodeServiceImpl implements BarcodeService {

    private final SecuenciaBarcodeRepository secuenciaBarcodeRepository;
    private final ContenedorRepository contenedorRepository;
    private final EmpresaRepository empresaRepository;

    @Override
    @Transactional
    public String generarBarcode(Long empresaId) {
        return generarBarcode(empresaId, InventarioConstantes.BARCODE_PREFIJO_DEFAULT);
    }

    @Override
    @Transactional
    public String generarBarcode(Long empresaId, String prefijo) {
        SecuenciaBarcode secuencia = secuenciaBarcodeRepository
                .findByEmpresaIdAndPrefijoForUpdate(empresaId, prefijo)
                .orElseGet(() -> crearSecuencia(empresaId, prefijo));

        long siguienteValor = secuencia.getUltimoValor() + 1;
        secuencia.setUltimoValor(siguienteValor);
        secuenciaBarcodeRepository.save(secuencia);

        String barcode = prefijo + String.format(
                "%0" + secuencia.getLongitudPadding() + "d", siguienteValor);

        log.debug("Barcode generado: {} para empresa {}", barcode, empresaId);
        return barcode;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeBarcode(Long empresaId, String codigoBarras) {
        return contenedorRepository.existsByEmpresaIdAndCodigoBarras(empresaId, codigoBarras);
    }

    private SecuenciaBarcode crearSecuencia(Long empresaId, String prefijo) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Empresa no encontrada: " + empresaId));

        SecuenciaBarcode nueva = SecuenciaBarcode.builder()
                .empresa(empresa)
                .prefijo(prefijo)
                .ultimoValor(0L)
                .longitudPadding(InventarioConstantes.BARCODE_LONGITUD_SECUENCIA)
                .build();

        return secuenciaBarcodeRepository.save(nueva);
    }
}

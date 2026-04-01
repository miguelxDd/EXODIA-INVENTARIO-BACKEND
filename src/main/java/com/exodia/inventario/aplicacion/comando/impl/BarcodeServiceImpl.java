package com.exodia.inventario.aplicacion.comando.impl;

import com.exodia.inventario.aplicacion.comando.BarcodeService;
import com.exodia.inventario.aplicacion.comando.ConfiguracionEmpresaService;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;
import com.exodia.inventario.domain.modelo.extension.ConfiguracionEmpresa;
import com.exodia.inventario.domain.modelo.extension.SecuenciaBarcode;
import com.exodia.inventario.excepcion.BarcodeDuplicadoException;
import com.exodia.inventario.repositorio.catalogo.EmpresaRepository;
import com.exodia.inventario.repositorio.contenedor.ContenedorRepository;
import com.exodia.inventario.repositorio.extension.SecuenciaBarcodeRepository;
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
    private final ConfiguracionEmpresaService configuracionEmpresaService;

    @Override
    @Transactional
    public String generarBarcode(Long empresaId) {
        ConfiguracionEmpresa config = configuracionEmpresaService.obtenerEntidadOCrear(empresaId);
        return generarBarcode(empresaId, config.getBarcodePrefijo());
    }

    @Override
    @Transactional
    public String generarBarcode(Long empresaId, String prefijo) {
        SecuenciaBarcode secuencia = secuenciaBarcodeRepository
                .findByEmpresaIdAndPrefijoForUpdate(empresaId, prefijo)
                .orElseGet(() -> crearSecuencia(empresaId, prefijo));

        // Sincronizar padding con ConfiguracionEmpresa si cambio
        ConfiguracionEmpresa config = configuracionEmpresaService.obtenerEntidadOCrear(empresaId);
        if (!secuencia.getLongitudPadding().equals(config.getBarcodeLongitudPadding())) {
            log.info("Actualizando longitudPadding de secuencia {}/{}: {} -> {}",
                    empresaId, prefijo, secuencia.getLongitudPadding(), config.getBarcodeLongitudPadding());
            secuencia.setLongitudPadding(config.getBarcodeLongitudPadding());
        }

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

        ConfiguracionEmpresa config = configuracionEmpresaService.obtenerEntidadOCrear(empresaId);
        SecuenciaBarcode nueva = SecuenciaBarcode.builder()
                .empresa(empresa)
                .prefijo(prefijo)
                .ultimoValor(0L)
                .longitudPadding(config.getBarcodeLongitudPadding())
                .build();

        return secuenciaBarcodeRepository.save(nueva);
    }
}

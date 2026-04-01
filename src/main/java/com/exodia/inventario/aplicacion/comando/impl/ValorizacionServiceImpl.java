package com.exodia.inventario.aplicacion.comando.impl;

import com.exodia.inventario.aplicacion.comando.ValorizacionService;
import com.exodia.inventario.aplicacion.consulta.StockQueryService;
import com.exodia.inventario.domain.enums.MetodoCosto;
import com.exodia.inventario.domain.modelo.catalogo.Bodega;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;
import com.exodia.inventario.domain.modelo.catalogo.Unidad;
import com.exodia.inventario.domain.modelo.extension.FotoCosto;
import com.exodia.inventario.domain.servicio.CalculadorCosto;
import com.exodia.inventario.excepcion.EntidadNoEncontradaException;
import com.exodia.inventario.interfaz.dto.respuesta.FotoCostoResponse;
import com.exodia.inventario.interfaz.mapeador.FotoCostoMapeador;
import com.exodia.inventario.repositorio.catalogo.BodegaRepository;
import com.exodia.inventario.repositorio.catalogo.EmpresaRepository;
import com.exodia.inventario.repositorio.catalogo.UnidadRepository;
import com.exodia.inventario.repositorio.extension.FotoCostoRepository;
import com.exodia.inventario.repositorio.proyeccion.ProductoBodegaStockProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ValorizacionServiceImpl implements ValorizacionService {

    private final FotoCostoRepository fotoCostoRepository;
    private final EmpresaRepository empresaRepository;
    private final BodegaRepository bodegaRepository;
    private final UnidadRepository unidadRepository;
    private final StockQueryService stockQueryService;
    private final CalculadorCosto calculadorCosto;
    private final FotoCostoMapeador fotoCostoMapeador;

    @Override
    @Transactional
    public void generarFotoCosto(Long empresaId) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EntidadNoEncontradaException("Empresa", empresaId));

        OffsetDateTime ahora = OffsetDateTime.now();

        List<ProductoBodegaStockProjection> stocks = stockQueryService
                .obtenerStockPorProductoBodega(empresaId, null, null);

        int registros = 0;
        for (ProductoBodegaStockProjection stock : stocks) {
            if (stock.getStockCantidad() == null
                    || stock.getStockCantidad().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            Bodega bodega = bodegaRepository.findById(stock.getBodegaId()).orElse(null);
            Unidad unidad = stock.getUnidadId() != null
                    ? unidadRepository.findById(stock.getUnidadId()).orElse(null)
                    : null;

            // Costo promedio ponderado desde contenedores con stock > 0
            BigDecimal costoUnitario = stockQueryService.obtenerCostoPromedioPonderado(
                    empresaId, stock.getProductoId(), stock.getBodegaId());
            BigDecimal costoTotal = calculadorCosto.calcularValorTotal(
                    stock.getStockCantidad(), costoUnitario);

            FotoCosto foto = FotoCosto.builder()
                    .empresa(empresa)
                    .productoId(stock.getProductoId())
                    .bodega(bodega)
                    .unidad(unidad)
                    .cantidadStock(stock.getStockCantidad())
                    .costoUnitario(costoUnitario)
                    .costoTotal(costoTotal)
                    .metodoCosto(MetodoCosto.PROMEDIO_PONDERADO)
                    .fechaFoto(ahora)
                    .build();

            fotoCostoRepository.save(foto);
            registros++;
        }

        log.info("Foto de costo generada para empresa {}: {} registros", empresaId, registros);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FotoCostoResponse> listarFotosCosto(Long empresaId, Pageable pageable) {
        return fotoCostoRepository.findByEmpresaIdOrderByFechaFotoDesc(empresaId, pageable)
                .map(fotoCostoMapeador::toResponse);
    }
}

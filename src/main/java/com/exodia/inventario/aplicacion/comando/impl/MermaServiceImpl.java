package com.exodia.inventario.aplicacion.comando.impl;

import com.exodia.inventario.aplicacion.comando.MermaService;
import com.exodia.inventario.aplicacion.comando.OperacionService;
import com.exodia.inventario.aplicacion.consulta.StockQueryService;
import com.exodia.inventario.domain.enums.TipoMerma;
import com.exodia.inventario.domain.enums.TipoOperacionCodigo;
import com.exodia.inventario.domain.enums.TipoReferencia;
import com.exodia.inventario.domain.modelo.contenedor.Contenedor;
import com.exodia.inventario.domain.modelo.contenedor.Operacion;
import com.exodia.inventario.domain.modelo.extension.ConfigMerma;
import com.exodia.inventario.domain.modelo.extension.RegistroMerma;
import com.exodia.inventario.domain.politica.PoliticaDeduccionStock;
import com.exodia.inventario.excepcion.EntidadNoEncontradaException;
import com.exodia.inventario.excepcion.OperacionInvalidaException;
import com.exodia.inventario.excepcion.StockInsuficienteException;
import com.exodia.inventario.interfaz.dto.peticion.CrearMermaRequest;
import com.exodia.inventario.interfaz.dto.respuesta.MermaResponse;
import com.exodia.inventario.interfaz.mapeador.MermaMapeador;
import com.exodia.inventario.domain.modelo.extension.ConfiguracionProducto;
import com.exodia.inventario.repositorio.contenedor.ContenedorRepository;
import com.exodia.inventario.repositorio.extension.ConfigMermaRepository;
import com.exodia.inventario.repositorio.extension.ConfiguracionProductoRepository;
import com.exodia.inventario.repositorio.extension.RegistroMermaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MermaServiceImpl implements MermaService {

    private final RegistroMermaRepository registroMermaRepository;
    private final ConfigMermaRepository configMermaRepository;
    private final ConfiguracionProductoRepository configuracionProductoRepository;
    private final ContenedorRepository contenedorRepository;
    private final OperacionService operacionService;
    private final StockQueryService stockQueryService;
    private final PoliticaDeduccionStock politicaDeduccionStock;
    private final MermaMapeador mermaMapeador;

    @Override
    @Transactional
    public MermaResponse registrar(Long empresaId, CrearMermaRequest request) {
        // Lock pesimista — la merma es una deduccion de stock
        Contenedor contenedor = contenedorRepository.findByIdForUpdate(request.contenedorId())
                .filter(c -> c.getEmpresa().getId().equals(empresaId))
                .orElseThrow(() -> new EntidadNoEncontradaException("Contenedor", request.contenedorId()));

        BigDecimal stockDisponible = stockQueryService.obtenerStockDisponible(contenedor.getId());

        PoliticaDeduccionStock.ResultadoValidacion resultado = politicaDeduccionStock.evaluar(
                contenedor.getEstado().getCodigo(), stockDisponible, request.cantidadMerma());

        if (!resultado.valido()) {
            throw new StockInsuficienteException(
                    contenedor.getId(), request.cantidadMerma(), stockDisponible);
        }

        // Buscar configuracion de merma aplicable (empresa+producto+bodega)
        ConfigMerma configMerma = buscarConfigMermaAplicable(
                empresaId, contenedor.getProductoId(),
                contenedor.getBodega() != null ? contenedor.getBodega().getId() : null);

        if (configMerma != null) {
            validarMermaContraConfig(configMerma, request.cantidadMerma(), stockDisponible,
                    empresaId, contenedor.getProductoId(),
                    contenedor.getBodega() != null ? contenedor.getBodega().getId() : null);
        } else {
            // Fallback: verificar toleranciaMerma del producto si existe
            validarToleranciaProducto(empresaId, contenedor.getProductoId(),
                    request.cantidadMerma(), stockDisponible);
        }

        // Usar tipoMerma de la config si existe, sino MANUAL
        TipoMerma tipoMermaResuelto = configMerma != null ? configMerma.getTipoMerma() : TipoMerma.MANUAL;

        Operacion operacion = operacionService.crearOperacion(
                contenedor,
                TipoOperacionCodigo.MERMA,
                request.cantidadMerma(),
                "Merma manual: " + (request.comentarios() != null ? request.comentarios() : ""));

        RegistroMerma registro = RegistroMerma.builder()
                .empresa(contenedor.getEmpresa())
                .contenedor(contenedor)
                .cantidadMerma(request.cantidadMerma())
                .tipoMerma(tipoMermaResuelto)
                .operacion(operacion)
                .configMerma(configMerma)
                .comentarios(request.comentarios())
                .build();

        registro = registroMermaRepository.save(registro);

        log.info("Merma registrada: contenedor={}, cantidad={}, empresa={}",
                contenedor.getCodigoBarras(), request.cantidadMerma(), empresaId);

        return mermaMapeador.toResponse(registro);
    }

    @Override
    @Transactional(readOnly = true)
    public MermaResponse obtenerPorId(Long empresaId, Long mermaId) {
        RegistroMerma registro = registroMermaRepository.findById(mermaId)
                .filter(r -> r.getEmpresa().getId().equals(empresaId))
                .orElseThrow(() -> new EntidadNoEncontradaException("RegistroMerma", mermaId));
        return mermaMapeador.toResponse(registro);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MermaResponse> listarPorEmpresa(Long empresaId, Pageable pageable) {
        return registroMermaRepository.findByEmpresaId(empresaId, pageable)
                .map(mermaMapeador::toResponse);
    }

    private ConfigMerma buscarConfigMermaAplicable(Long empresaId, Long productoId, Long bodegaId) {
        // Buscar config especifica (empresa+producto+bodega)
        return configMermaRepository.findFirstByEmpresaIdAndActivoTrueAndProductoIdAndBodegaIdOrderByIdAsc(
                        empresaId, productoId, bodegaId)
                // Fallback: config por producto sin bodega
                .or(() -> configMermaRepository.findFirstByEmpresaIdAndActivoTrueAndProductoIdAndBodegaIdOrderByIdAsc(
                        empresaId, productoId, null))
                // Fallback: config global de empresa
                .or(() -> configMermaRepository.findFirstByEmpresaIdAndActivoTrueAndProductoIdAndBodegaIdOrderByIdAsc(
                        empresaId, null, null))
                .orElse(null);
    }

    private void validarMermaContraConfig(ConfigMerma config, BigDecimal cantidadMerma,
                                            BigDecimal stockDisponible,
                                            Long empresaId, Long productoId, Long bodegaId) {
        if (config.getPorcentajeMerma() != null && stockDisponible.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal limitePorc = stockDisponible.multiply(config.getPorcentajeMerma())
                    .divide(BigDecimal.valueOf(100), 6, java.math.RoundingMode.HALF_UP);
            if (cantidadMerma.compareTo(limitePorc) > 0) {
                throw new OperacionInvalidaException(String.format(
                        "Merma (%s) excede el porcentaje configurado (%s%% = %s) para config %d",
                        cantidadMerma, config.getPorcentajeMerma(), limitePorc, config.getId()));
            }
        }
        if (config.getCantidadFijaMerma() != null) {
            if (cantidadMerma.compareTo(config.getCantidadFijaMerma()) > 0) {
                throw new OperacionInvalidaException(String.format(
                        "Merma (%s) excede la cantidad fija configurada (%s) para config %d",
                        cantidadMerma, config.getCantidadFijaMerma(), config.getId()));
            }
        }
        // Validar frecuencia: si hay frecuenciaDias, no permitir merma si ya se registro una en ese periodo
        if (config.getFrecuenciaDias() != null && config.getFrecuenciaDias() > 0) {
            OffsetDateTime desde = OffsetDateTime.now().minusDays(config.getFrecuenciaDias());
            registroMermaRepository.findUltimaMermaDentroDeVentana(empresaId, productoId, bodegaId, desde)
                    .ifPresent(ultimaMerma -> {
                        throw new OperacionInvalidaException(String.format(
                                "Ya se registro merma para producto %d dentro de los ultimos %d dias (ultima: %s)",
                                productoId, config.getFrecuenciaDias(), ultimaMerma.getCreadoEn()));
                    });
        }
    }

    private void validarToleranciaProducto(Long empresaId, Long productoId,
                                            BigDecimal cantidadMerma, BigDecimal stockDisponible) {
        configuracionProductoRepository.findByEmpresaIdAndProductoId(empresaId, productoId)
                .ifPresent(configProd -> {
                    if (configProd.getToleranciaMerma() != null
                            && configProd.getToleranciaMerma().compareTo(BigDecimal.ZERO) > 0
                            && stockDisponible.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal limite = stockDisponible.multiply(configProd.getToleranciaMerma())
                                .divide(BigDecimal.valueOf(100), 6, java.math.RoundingMode.HALF_UP);
                        if (cantidadMerma.compareTo(limite) > 0) {
                            throw new OperacionInvalidaException(String.format(
                                    "Merma (%s) excede la tolerancia del producto (%s%% = %s)",
                                    cantidadMerma, configProd.getToleranciaMerma(), limite));
                        }
                    }
                });
    }
}

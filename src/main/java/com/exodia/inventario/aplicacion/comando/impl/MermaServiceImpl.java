package com.exodia.inventario.aplicacion.comando.impl;

import com.exodia.inventario.aplicacion.comando.MermaService;
import com.exodia.inventario.aplicacion.comando.OperacionService;
import com.exodia.inventario.domain.enums.MotivoMermaCodigo;
import com.exodia.inventario.aplicacion.consulta.StockQueryService;
import com.exodia.inventario.domain.evento.MermaRegistradaEvent;
import com.exodia.inventario.domain.enums.TipoMerma;
import com.exodia.inventario.domain.enums.TipoOperacionCodigo;
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
import com.exodia.inventario.repositorio.contenedor.ContenedorRepository;
import com.exodia.inventario.repositorio.extension.ConfigMermaRepository;
import com.exodia.inventario.repositorio.extension.ConfiguracionProductoRepository;
import com.exodia.inventario.repositorio.extension.RegistroMermaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public MermaResponse registrar(Long empresaId, CrearMermaRequest request) {
        return registrarMerma(
                empresaId,
                request.contenedorId(),
                request.cantidadMerma(),
                request.motivoCodigo(),
                request.comentarios(),
                TipoMerma.MANUAL,
                MotivoMermaCodigo.OTRO,
                "Merma manual");
    }

    @Override
    @Transactional
    public MermaResponse registrarAutomaticaEnRecepcion(Long empresaId,
                                                        Long contenedorId,
                                                        BigDecimal cantidadMerma,
                                                        String comentarios) {
        return registrarMerma(
                empresaId,
                contenedorId,
                cantidadMerma,
                null,
                comentarios,
                TipoMerma.AUTOMATICA,
                MotivoMermaCodigo.RECEPCION,
                "Merma automatica en recepcion");
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

    private MermaResponse registrarMerma(Long empresaId,
                                         Long contenedorId,
                                         BigDecimal cantidadMerma,
                                         MotivoMermaCodigo motivoSolicitado,
                                         String comentarios,
                                         TipoMerma tipoMermaPorDefecto,
                                         MotivoMermaCodigo motivoPorDefecto,
                                         String descripcionOperacion) {
        if (cantidadMerma == null || cantidadMerma.compareTo(BigDecimal.ZERO) <= 0) {
            throw new OperacionInvalidaException(
                    "La cantidad de merma debe ser mayor a cero");
        }

        Contenedor contenedor = contenedorRepository.findByIdForUpdate(contenedorId)
                .filter(c -> c.getEmpresa().getId().equals(empresaId))
                .orElseThrow(() -> new EntidadNoEncontradaException("Contenedor", contenedorId));

        BigDecimal stockDisponible = stockQueryService.obtenerStockDisponible(contenedor.getId());

        PoliticaDeduccionStock.ResultadoValidacion resultado = politicaDeduccionStock.evaluar(
                contenedor.getEstado().getCodigo(), stockDisponible, cantidadMerma);

        if (!resultado.valido()) {
            throw new StockInsuficienteException(
                    contenedor.getId(), cantidadMerma, stockDisponible);
        }

        ConfigMerma configMerma = buscarConfigMermaAplicable(
                empresaId, contenedor.getProductoId(),
                contenedor.getBodega() != null ? contenedor.getBodega().getId() : null);

        if (configMerma != null) {
            validarMermaContraConfig(configMerma, cantidadMerma, stockDisponible,
                    empresaId, contenedor.getProductoId(),
                    contenedor.getBodega() != null ? contenedor.getBodega().getId() : null);
        } else {
            validarToleranciaProducto(empresaId, contenedor.getProductoId(),
                    cantidadMerma, stockDisponible);
        }

        TipoMerma tipoMermaResuelto = configMerma != null && configMerma.getTipoMerma() != null
                ? configMerma.getTipoMerma()
                : tipoMermaPorDefecto;
        MotivoMermaCodigo motivoResuelto = motivoSolicitado != null
                ? motivoSolicitado
                : motivoPorDefecto;

        Operacion operacion = operacionService.crearOperacion(
                contenedor,
                TipoOperacionCodigo.MERMA,
                cantidadMerma,
                construirDescripcionOperacion(descripcionOperacion, motivoResuelto, comentarios));

        RegistroMerma registro = RegistroMerma.builder()
                .empresa(contenedor.getEmpresa())
                .contenedor(contenedor)
                .cantidadMerma(cantidadMerma)
                .tipoMerma(tipoMermaResuelto)
                .motivoCodigo(motivoResuelto)
                .operacion(operacion)
                .configMerma(configMerma)
                .comentarios(comentarios)
                .build();

        registro = registroMermaRepository.save(registro);

        log.info("Merma registrada: contenedor={}, cantidad={}, empresa={}, tipo={}",
                contenedor.getCodigoBarras(), cantidadMerma, empresaId, tipoMermaResuelto);

        eventPublisher.publishEvent(new MermaRegistradaEvent(
                registro.getId(),
                empresaId,
                contenedor.getId(),
                contenedor.getProductoId(),
                contenedor.getBodega() != null ? contenedor.getBodega().getId() : null,
                cantidadMerma,
                tipoMermaResuelto.name()));

        return mermaMapeador.toResponse(registro);
    }

    private String construirDescripcionOperacion(String descripcionBase,
                                                 MotivoMermaCodigo motivoCodigo,
                                                 String comentarios) {
        String descripcion = descripcionBase;
        if (motivoCodigo != null) {
            descripcion = descripcion + " [" + motivoCodigo.name() + "]";
        }
        if (comentarios == null || comentarios.isBlank()) {
            return descripcion;
        }
        return descripcion + ": " + comentarios;
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

package com.exodia.inventario.aplicacion.comando.impl;

import com.exodia.inventario.aplicacion.comando.AjusteInventarioService;
import com.exodia.inventario.aplicacion.comando.BarcodeService;
import com.exodia.inventario.aplicacion.comando.OperacionService;
import com.exodia.inventario.aplicacion.consulta.StockQueryService;
import com.exodia.inventario.domain.enums.TipoOperacionCodigo;
import com.exodia.inventario.domain.enums.TipoReferencia;
import com.exodia.inventario.domain.evento.StockAjustadoEvent;
import com.exodia.inventario.domain.modelo.ajuste.Ajuste;
import com.exodia.inventario.domain.modelo.ajuste.AjusteLinea;
import com.exodia.inventario.domain.modelo.catalogo.Bodega;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;
import com.exodia.inventario.domain.modelo.catalogo.TipoAjuste;
import com.exodia.inventario.domain.modelo.contenedor.Contenedor;
import com.exodia.inventario.domain.modelo.contenedor.Operacion;
import com.exodia.inventario.domain.politica.PoliticaDeduccionStock;
import com.exodia.inventario.excepcion.EntidadNoEncontradaException;
import com.exodia.inventario.excepcion.OperacionInvalidaException;
import com.exodia.inventario.excepcion.StockInsuficienteException;
import com.exodia.inventario.interfaz.dto.peticion.AjusteLineaRequest;
import com.exodia.inventario.interfaz.dto.peticion.CrearAjusteRequest;
import com.exodia.inventario.interfaz.dto.respuesta.AjusteResponse;
import com.exodia.inventario.interfaz.mapeador.AjusteMapeador;
import com.exodia.inventario.repositorio.ajuste.AjusteRepository;
import com.exodia.inventario.repositorio.catalogo.BodegaRepository;
import com.exodia.inventario.repositorio.catalogo.EmpresaRepository;
import com.exodia.inventario.repositorio.catalogo.TipoAjusteRepository;
import com.exodia.inventario.repositorio.contenedor.ContenedorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class AjusteInventarioServiceImpl implements AjusteInventarioService {

    private final AjusteRepository ajusteRepository;
    private final EmpresaRepository empresaRepository;
    private final BodegaRepository bodegaRepository;
    private final TipoAjusteRepository tipoAjusteRepository;
    private final ContenedorRepository contenedorRepository;
    private final OperacionService operacionService;
    private final StockQueryService stockQueryService;
    private final BarcodeService barcodeService;
    private final AjusteMapeador ajusteMapeador;
    private final ApplicationEventPublisher eventPublisher;
    private final PoliticaDeduccionStock politicaDeduccionStock;

    @Override
    @Transactional
    public AjusteResponse crear(Long empresaId, CrearAjusteRequest request) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EntidadNoEncontradaException("Empresa", empresaId));

        Bodega bodega = bodegaRepository.findById(request.bodegaId())
                .filter(b -> b.getEmpresa().getId().equals(empresaId))
                .filter(b -> Boolean.TRUE.equals(b.getActivo()))
                .orElseThrow(() -> new EntidadNoEncontradaException("Bodega", request.bodegaId()));

        TipoAjuste tipoAjuste = tipoAjusteRepository.findByCodigo(request.tipoAjusteCodigo())
                .orElseThrow(() -> new OperacionInvalidaException(
                        "Tipo de ajuste no encontrado: " + request.tipoAjusteCodigo()));

        String numeroAjuste = barcodeService.generarBarcode(empresaId, "AJU");

        Ajuste ajuste = Ajuste.builder()
                .empresa(empresa)
                .numeroAjuste(numeroAjuste)
                .bodega(bodega)
                .tipoAjuste(tipoAjuste)
                .motivo(request.motivo())
                .tipoReferencia(TipoReferencia.AJUSTE)
                .build();

        ajuste = ajusteRepository.save(ajuste);

        for (AjusteLineaRequest lineaReq : request.lineas()) {
            AjusteLinea linea = procesarLinea(ajuste, empresa, lineaReq);
            ajuste.getLineas().add(linea);
        }

        ajuste = ajusteRepository.save(ajuste);

        log.info("Ajuste {} creado con {} lineas en bodega {} para empresa {}",
                numeroAjuste, request.lineas().size(), bodega.getCodigo(), empresaId);

        return ajusteMapeador.toResponse(ajuste);
    }

    @Override
    @Transactional(readOnly = true)
    public AjusteResponse obtenerPorId(Long empresaId, Long ajusteId) {
        Ajuste ajuste = ajusteRepository.findById(ajusteId)
                .filter(a -> a.getEmpresa().getId().equals(empresaId))
                .filter(a -> Boolean.TRUE.equals(a.getActivo()))
                .orElseThrow(() -> new EntidadNoEncontradaException("Ajuste", ajusteId));
        return ajusteMapeador.toResponse(ajuste);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AjusteResponse> listarPorEmpresa(Long empresaId, Pageable pageable) {
        return ajusteRepository.findByEmpresaIdOrderByCreadoEnDesc(empresaId, pageable)
                .map(ajusteMapeador::toResponse);
    }

    private AjusteLinea procesarLinea(Ajuste ajuste, Empresa empresa, AjusteLineaRequest lineaReq) {
        BigDecimal stockActual = stockQueryService.obtenerStockContenedor(lineaReq.contenedorId());
        BigDecimal precioActual;
        Contenedor contenedor;

        boolean esDeduccion = lineaReq.cantidadNueva() != null
                && lineaReq.cantidadNueva().compareTo(stockActual) < 0;

        if (esDeduccion) {
            // Lock pesimista para deducciones de stock
            contenedor = contenedorRepository.findByIdForUpdate(lineaReq.contenedorId())
                    .filter(c -> c.getEmpresa().getId().equals(empresa.getId()))
                    .orElseThrow(() -> new EntidadNoEncontradaException("Contenedor", lineaReq.contenedorId()));

            // Recalcular stock con lock adquirido
            stockActual = stockQueryService.obtenerStockContenedor(lineaReq.contenedorId());
            BigDecimal cantidadDeducir = stockActual.subtract(lineaReq.cantidadNueva());
            BigDecimal stockDisponible = stockQueryService.obtenerStockDisponible(lineaReq.contenedorId());

            // Validar con politica de deduccion
            PoliticaDeduccionStock.ResultadoValidacion resultado = politicaDeduccionStock.evaluar(
                    contenedor.getEstado().getCodigo(), stockDisponible, cantidadDeducir);

            if (!resultado.valido()) {
                throw new StockInsuficienteException(
                        lineaReq.contenedorId(), cantidadDeducir, stockDisponible);
            }
        } else {
            contenedor = contenedorRepository.findById(lineaReq.contenedorId())
                    .filter(c -> c.getEmpresa().getId().equals(empresa.getId()))
                    .orElseThrow(() -> new EntidadNoEncontradaException("Contenedor", lineaReq.contenedorId()));
        }

        precioActual = contenedor.getPrecioUnitario();

        BigDecimal cantidadAjuste = BigDecimal.ZERO;
        Operacion operacion = null;

        // Ajuste de cantidad
        if (lineaReq.cantidadNueva() != null) {
            cantidadAjuste = lineaReq.cantidadNueva().subtract(stockActual);

            if (cantidadAjuste.compareTo(BigDecimal.ZERO) != 0) {
                TipoOperacionCodigo tipoCodigo = cantidadAjuste.compareTo(BigDecimal.ZERO) > 0
                        ? TipoOperacionCodigo.AJUSTE_POSITIVO
                        : TipoOperacionCodigo.AJUSTE_NEGATIVO;

                operacion = operacionService.crearOperacion(
                        contenedor,
                        tipoCodigo,
                        cantidadAjuste.abs(),
                        "Ajuste " + ajuste.getNumeroAjuste() + ": " + ajuste.getMotivo(),
                        TipoReferencia.AJUSTE,
                        ajuste.getId(),
                        null);

                // Publicar evento
                eventPublisher.publishEvent(new StockAjustadoEvent(
                        ajuste.getId(), empresa.getId(), contenedor.getId(),
                        contenedor.getProductoId(), contenedor.getBodega().getId(),
                        stockActual, lineaReq.cantidadNueva()));
            }
        }

        // Ajuste de precio
        if (lineaReq.precioNuevo() != null
                && lineaReq.precioNuevo().compareTo(precioActual) != 0) {
            contenedor.setPrecioUnitario(lineaReq.precioNuevo());
            contenedorRepository.save(contenedor);

            // Si no hubo ajuste de cantidad, registrar operacion informativa
            if (operacion == null) {
                operacion = operacionService.crearOperacion(
                        contenedor,
                        TipoOperacionCodigo.AJUSTE_INFORMATIVO,
                        BigDecimal.ONE,
                        "Ajuste precio " + ajuste.getNumeroAjuste()
                                + ": " + precioActual.toPlainString()
                                + " -> " + lineaReq.precioNuevo().toPlainString(),
                        TipoReferencia.AJUSTE,
                        ajuste.getId(),
                        null);
            }
        }

        return AjusteLinea.builder()
                .ajuste(ajuste)
                .contenedor(contenedor)
                .cantidadAnterior(stockActual)
                .cantidadNueva(lineaReq.cantidadNueva())
                .cantidadAjuste(cantidadAjuste)
                .precioAnterior(precioActual)
                .precioNuevo(lineaReq.precioNuevo())
                .operacion(operacion)
                .build();
    }
}

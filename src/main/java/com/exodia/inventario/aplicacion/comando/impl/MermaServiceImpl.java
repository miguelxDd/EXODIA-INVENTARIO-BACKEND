package com.exodia.inventario.aplicacion.comando.impl;

import com.exodia.inventario.aplicacion.comando.MermaService;
import com.exodia.inventario.aplicacion.comando.OperacionService;
import com.exodia.inventario.aplicacion.consulta.StockQueryService;
import com.exodia.inventario.domain.enums.TipoMerma;
import com.exodia.inventario.domain.enums.TipoOperacionCodigo;
import com.exodia.inventario.domain.enums.TipoReferencia;
import com.exodia.inventario.domain.modelo.contenedor.Contenedor;
import com.exodia.inventario.domain.modelo.contenedor.Operacion;
import com.exodia.inventario.domain.modelo.extension.RegistroMerma;
import com.exodia.inventario.domain.politica.PoliticaDeduccionStock;
import com.exodia.inventario.excepcion.EntidadNoEncontradaException;
import com.exodia.inventario.excepcion.StockInsuficienteException;
import com.exodia.inventario.interfaz.dto.peticion.CrearMermaRequest;
import com.exodia.inventario.interfaz.dto.respuesta.MermaResponse;
import com.exodia.inventario.interfaz.mapeador.MermaMapeador;
import com.exodia.inventario.repositorio.contenedor.ContenedorRepository;
import com.exodia.inventario.repositorio.extension.RegistroMermaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class MermaServiceImpl implements MermaService {

    private final RegistroMermaRepository registroMermaRepository;
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

        Operacion operacion = operacionService.crearOperacion(
                contenedor,
                TipoOperacionCodigo.MERMA,
                request.cantidadMerma(),
                "Merma manual: " + (request.comentarios() != null ? request.comentarios() : ""));

        RegistroMerma registro = RegistroMerma.builder()
                .empresa(contenedor.getEmpresa())
                .contenedor(contenedor)
                .cantidadMerma(request.cantidadMerma())
                .tipoMerma(TipoMerma.MANUAL)
                .operacion(operacion)
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
}

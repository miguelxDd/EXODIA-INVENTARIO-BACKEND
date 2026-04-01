package com.exodia.inventario.aplicacion.comando.impl;

import com.exodia.inventario.aplicacion.comando.ConfiguracionEmpresaService;
import com.exodia.inventario.aplicacion.comando.ReservaService;
import com.exodia.inventario.aplicacion.consulta.StockQueryService;
import com.exodia.inventario.domain.modelo.contenedor.Contenedor;
import com.exodia.inventario.domain.modelo.contenedor.Reserva;
import com.exodia.inventario.domain.modelo.extension.ConfiguracionEmpresa;
import com.exodia.inventario.domain.politica.PoliticaReserva;
import com.exodia.inventario.excepcion.EntidadNoEncontradaException;
import com.exodia.inventario.excepcion.OperacionInvalidaException;
import com.exodia.inventario.interfaz.dto.peticion.CrearReservaRequest;
import com.exodia.inventario.interfaz.dto.respuesta.ReservaResponse;
import com.exodia.inventario.interfaz.mapeador.ReservaMapeador;
import com.exodia.inventario.repositorio.contenedor.ContenedorRepository;
import com.exodia.inventario.repositorio.contenedor.ReservaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static com.exodia.inventario.util.InventarioConstantes.ESTADO_RESERVA_CANCELADA;
import static com.exodia.inventario.util.InventarioConstantes.ESTADO_RESERVA_PENDIENTE;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservaServiceImpl implements ReservaService {

    private final ReservaRepository reservaRepository;
    private final ContenedorRepository contenedorRepository;
    private final StockQueryService stockQueryService;
    private final ConfiguracionEmpresaService configuracionEmpresaService;
    private final PoliticaReserva politicaReserva;
    private final ReservaMapeador reservaMapeador;

    @Override
    @Transactional
    public ReservaResponse crear(Long empresaId, CrearReservaRequest request) {
        // Lock pesimista para validar stock disponible
        Contenedor contenedor = contenedorRepository.findByIdForUpdate(request.contenedorId())
                .filter(c -> c.getEmpresa().getId().equals(empresaId))
                .orElseThrow(() -> new EntidadNoEncontradaException("Contenedor", request.contenedorId()));

        BigDecimal stockDisponible = stockQueryService.obtenerStockDisponible(contenedor.getId());

        ConfiguracionEmpresa configEmpresa = configuracionEmpresaService.obtenerEntidadOCrear(empresaId);
        OffsetDateTime fechaExpiracion = request.fechaExpiracion() != null
                ? request.fechaExpiracion()
                : OffsetDateTime.now().plusHours(configEmpresa.getExpiracionReservaHoras());

        // Validar con politica de reserva
        PoliticaReserva.ResultadoValidacion resultado = politicaReserva.evaluar(
                stockDisponible, request.cantidadReservada(), fechaExpiracion);

        if (!resultado.valido()) {
            throw new OperacionInvalidaException(resultado.razon());
        }

        Reserva reserva = Reserva.builder()
                .empresa(contenedor.getEmpresa())
                .contenedor(contenedor)
                .codigoBarras(contenedor.getCodigoBarras())
                .productoId(contenedor.getProductoId())
                .bodega(contenedor.getBodega())
                .cantidadReservada(request.cantidadReservada())
                .estado(ESTADO_RESERVA_PENDIENTE)
                .tipoReferencia(request.tipoReferencia())
                .referenciaId(request.referenciaId())
                .referenciaLineaId(request.referenciaLineaId())
                .fechaExpiracion(fechaExpiracion)
                .build();

        reserva = reservaRepository.save(reserva);

        log.info("Reserva {} creada: contenedor={}, cantidad={}, referencia={}/{}",
                reserva.getId(), contenedor.getCodigoBarras(),
                request.cantidadReservada(), request.tipoReferencia(), request.referenciaId());

        return reservaMapeador.toResponse(reserva);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservaResponse obtenerPorId(Long empresaId, Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .filter(r -> r.getEmpresa().getId().equals(empresaId))
                .orElseThrow(() -> new EntidadNoEncontradaException("Reserva", reservaId));
        return reservaMapeador.toResponse(reserva);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaResponse> listarPorContenedor(Long empresaId, Long contenedorId) {
        List<Reserva> reservas = reservaRepository.findByContenedorIdAndEmpresaIdAndEstadoIn(
                contenedorId, empresaId, List.of(ESTADO_RESERVA_PENDIENTE, "PARCIAL"));
        return reservaMapeador.toResponseList(reservas);
    }

    @Override
    @Transactional
    public ReservaResponse cancelar(Long empresaId, Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .filter(r -> r.getEmpresa().getId().equals(empresaId))
                .orElseThrow(() -> new EntidadNoEncontradaException("Reserva", reservaId));

        if (ESTADO_RESERVA_CANCELADA.equals(reserva.getEstado())
                || "CUMPLIDA".equals(reserva.getEstado())) {
            throw new OperacionInvalidaException(
                    "No se puede cancelar una reserva en estado: " + reserva.getEstado());
        }

        reserva.setEstado(ESTADO_RESERVA_CANCELADA);
        reserva = reservaRepository.save(reserva);

        log.info("Reserva {} cancelada", reservaId);

        return reservaMapeador.toResponse(reserva);
    }
}

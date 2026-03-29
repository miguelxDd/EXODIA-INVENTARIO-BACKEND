package com.exodia.inventario.domain.servicio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Servicio de dominio puro (sin Spring) que implementa la politica FEFO
 * (First Expired, First Out) para seleccion de contenedores en picking y transferencias.
 *
 * Orden: fecha de vencimiento ASC (nulls last), luego fecha de creacion ASC.
 */
public class PoliticaFEFO {

    /**
     * Ordena contenedores disponibles segun FEFO.
     *
     * @param disponibles lista de contenedores con stock disponible
     * @return lista de IDs ordenados segun FEFO
     */
    public List<Long> ordenarContenedoresParaPicking(List<ContenedorConStock> disponibles) {
        if (disponibles == null || disponibles.isEmpty()) {
            return List.of();
        }

        return disponibles.stream()
                .sorted(Comparator
                        .comparing(ContenedorConStock::fechaVencimiento,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(ContenedorConStock::creadoEn,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                .map(ContenedorConStock::id)
                .toList();
    }

    /**
     * Selecciona contenedores para cubrir una cantidad solicitada usando FEFO.
     * Retorna pares (contenedorId, cantidadADeducir).
     *
     * @param disponibles contenedores ordenados por FEFO (o sin ordenar; se ordenan internamente)
     * @param cantidadSolicitada cantidad total a cubrir
     * @return lista de asignaciones (contenedorId, cantidad a tomar de ese contenedor)
     */
    public List<AsignacionContenedor> seleccionarContenedores(
            List<ContenedorConStock> disponibles,
            BigDecimal cantidadSolicitada) {

        if (disponibles == null || disponibles.isEmpty() || cantidadSolicitada == null
                || cantidadSolicitada.compareTo(BigDecimal.ZERO) <= 0) {
            return List.of();
        }

        List<ContenedorConStock> ordenados = disponibles.stream()
                .sorted(Comparator
                        .comparing(ContenedorConStock::fechaVencimiento,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(ContenedorConStock::creadoEn,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        var resultado = new java.util.ArrayList<AsignacionContenedor>();
        BigDecimal pendiente = cantidadSolicitada;

        for (ContenedorConStock c : ordenados) {
            if (pendiente.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            BigDecimal disponible = c.cantidadDisponible();
            if (disponible.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            BigDecimal tomar = pendiente.min(disponible);
            resultado.add(new AsignacionContenedor(c.id(), tomar));
            pendiente = pendiente.subtract(tomar);
        }

        return List.copyOf(resultado);
    }

    /**
     * DTO interno que representa un contenedor con su stock disponible.
     */
    public record ContenedorConStock(
            Long id,
            LocalDate fechaVencimiento,
            OffsetDateTime creadoEn,
            BigDecimal cantidadDisponible
    ) {}

    /**
     * Resultado de la seleccion: contenedor y cantidad asignada.
     */
    public record AsignacionContenedor(
            Long contenedorId,
            BigDecimal cantidad
    ) {}
}

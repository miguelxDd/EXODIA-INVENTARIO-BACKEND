package com.exodia.inventario.unit.dominio.servicio;

import com.exodia.inventario.domain.enums.EstadoTransferenciaCodigo;
import com.exodia.inventario.domain.servicio.ValidadorEstadoTransferencia;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class ValidadorEstadoTransferenciaTest {

    private ValidadorEstadoTransferencia validador;

    @BeforeEach
    void setUp() {
        validador = new ValidadorEstadoTransferencia();
    }

    @Test
    void deberiaPermitirBorradorAConfirmado() {
        assertTrue(validador.esTransicionValida(
                EstadoTransferenciaCodigo.BORRADOR,
                EstadoTransferenciaCodigo.CONFIRMADO));
    }

    @Test
    void deberiaPermitirBorradorACancelado() {
        assertTrue(validador.esTransicionValida(
                EstadoTransferenciaCodigo.BORRADOR,
                EstadoTransferenciaCodigo.CANCELADO));
    }

    @Test
    void deberiaPermitirConfirmadoADespachado() {
        assertTrue(validador.esTransicionValida(
                EstadoTransferenciaCodigo.CONFIRMADO,
                EstadoTransferenciaCodigo.DESPACHADO));
    }

    @Test
    void deberiaPermitirDespachadoAEnTransito() {
        assertTrue(validador.esTransicionValida(
                EstadoTransferenciaCodigo.DESPACHADO,
                EstadoTransferenciaCodigo.EN_TRANSITO));
    }

    @Test
    void deberiaPermitirEnTransitoARecibidoParcial() {
        assertTrue(validador.esTransicionValida(
                EstadoTransferenciaCodigo.EN_TRANSITO,
                EstadoTransferenciaCodigo.RECIBIDO_PARCIAL));
    }

    @Test
    void deberiaPermitirEnTransitoARecibidoCompleto() {
        assertTrue(validador.esTransicionValida(
                EstadoTransferenciaCodigo.EN_TRANSITO,
                EstadoTransferenciaCodigo.RECIBIDO_COMPLETO));
    }

    @Test
    void deberiaPermitirRecibidoParcialACiereForzado() {
        assertTrue(validador.esTransicionValida(
                EstadoTransferenciaCodigo.RECIBIDO_PARCIAL,
                EstadoTransferenciaCodigo.CIERRE_FORZADO));
    }

    @Test
    void deberiaRechazarTransicionInvalida() {
        assertFalse(validador.esTransicionValida(
                EstadoTransferenciaCodigo.BORRADOR,
                EstadoTransferenciaCodigo.RECIBIDO_COMPLETO));
    }

    @Test
    void deberiaRechazarTransicionDesdeEstadoFinal() {
        assertFalse(validador.esTransicionValida(
                EstadoTransferenciaCodigo.RECIBIDO_COMPLETO,
                EstadoTransferenciaCodigo.BORRADOR));
        assertFalse(validador.esTransicionValida(
                EstadoTransferenciaCodigo.CANCELADO,
                EstadoTransferenciaCodigo.BORRADOR));
    }

    @Test
    void deberiaRechazarNulos() {
        assertFalse(validador.esTransicionValida(null, EstadoTransferenciaCodigo.CONFIRMADO));
        assertFalse(validador.esTransicionValida(EstadoTransferenciaCodigo.BORRADOR, null));
    }

    @Test
    void deberiaLanzarExcepcionEnTransicionInvalida() {
        assertThrows(IllegalStateException.class, () ->
                validador.validarTransicion(
                        EstadoTransferenciaCodigo.BORRADOR,
                        EstadoTransferenciaCodigo.EN_TRANSITO));
    }

    @Test
    void deberiaRetornarTransicionesPermitidas() {
        Set<EstadoTransferenciaCodigo> permitidas =
                validador.obtenerTransicionesPermitidas(EstadoTransferenciaCodigo.BORRADOR);

        assertTrue(permitidas.contains(EstadoTransferenciaCodigo.CONFIRMADO));
        assertTrue(permitidas.contains(EstadoTransferenciaCodigo.CANCELADO));
        assertEquals(2, permitidas.size());
    }

    @Test
    void deberiaVerificarCancelacionPermitida() {
        assertTrue(validador.permiteCancelacion(EstadoTransferenciaCodigo.BORRADOR));
        assertTrue(validador.permiteCancelacion(EstadoTransferenciaCodigo.CONFIRMADO));
        assertFalse(validador.permiteCancelacion(EstadoTransferenciaCodigo.DESPACHADO));
        assertFalse(validador.permiteCancelacion(EstadoTransferenciaCodigo.EN_TRANSITO));
    }
}

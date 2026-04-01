package com.exodia.inventario.config;

import com.exodia.inventario.domain.politica.PoliticaDeduccionStock;
import com.exodia.inventario.domain.politica.PoliticaReserva;
import com.exodia.inventario.domain.servicio.CalculadorCosto;
import com.exodia.inventario.domain.servicio.CalculadorStock;
import com.exodia.inventario.domain.servicio.PoliticaFEFO;
import com.exodia.inventario.domain.servicio.ValidadorEstadoTransferencia;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuracion general del modulo de inventario.
 * Registra los servicios de dominio puros como beans de Spring.
 */
@Configuration
@EnableAsync
@EnableScheduling
public class InventarioConfig {

    @Bean
    public CalculadorStock calculadorStock() {
        return new CalculadorStock();
    }

    @Bean
    public PoliticaFEFO politicaFEFO() {
        return new PoliticaFEFO();
    }

    @Bean
    public ValidadorEstadoTransferencia validadorEstadoTransferencia() {
        return new ValidadorEstadoTransferencia();
    }

    @Bean
    public CalculadorCosto calculadorCosto() {
        return new CalculadorCosto();
    }

    @Bean
    public PoliticaDeduccionStock politicaDeduccionStock() {
        return new PoliticaDeduccionStock();
    }

    @Bean
    public PoliticaReserva politicaReserva() {
        return new PoliticaReserva();
    }
}

package com.exodia.inventario.infraestructura.programacion;

import com.exodia.inventario.aplicacion.comando.ValorizacionService;
import com.exodia.inventario.domain.modelo.catalogo.Empresa;
import com.exodia.inventario.repositorio.catalogo.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FotoCostoMensualJob {

    private final ValorizacionService valorizacionService;
    private final EmpresaRepository empresaRepository;

    @Scheduled(cron = "0 0 3 1 * ?")
    @Transactional
    public void generarFotosCostoMensuales() {
        log.info("Iniciando generacion mensual de fotos de costo");

        List<Empresa> empresas = empresaRepository.findAll();
        int total = 0;

        for (Empresa empresa : empresas) {
            if (!Boolean.TRUE.equals(empresa.getActivo())) {
                continue;
            }
            try {
                valorizacionService.generarFotoCosto(empresa.getId());
                total++;
            } catch (Exception e) {
                log.error("Error generando foto de costo para empresa {}: {}",
                        empresa.getId(), e.getMessage());
            }
        }

        log.info("Fotos de costo mensuales generadas para {} empresas", total);
    }
}

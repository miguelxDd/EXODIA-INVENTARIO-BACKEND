package com.exodia.inventario.aplicacion.comando;

import com.exodia.inventario.interfaz.dto.peticion.CrearMermaRequest;
import com.exodia.inventario.interfaz.dto.respuesta.MermaResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MermaService {

    MermaResponse registrar(Long empresaId, CrearMermaRequest request);

    MermaResponse registrarAutomaticaEnRecepcion(Long empresaId,
                                                 Long contenedorId,
                                                 java.math.BigDecimal cantidadMerma,
                                                 String comentarios);

    MermaResponse obtenerPorId(Long empresaId, Long mermaId);

    Page<MermaResponse> listarPorEmpresa(Long empresaId, Pageable pageable);
}

package com.exodia.inventario.interfaz.rest.extension;

import com.exodia.inventario.aplicacion.comando.MermaService;
import com.exodia.inventario.interfaz.dto.peticion.CrearMermaRequest;
import com.exodia.inventario.interfaz.dto.respuesta.ApiResponse;
import com.exodia.inventario.interfaz.dto.respuesta.MermaResponse;
import com.exodia.inventario.interfaz.dto.respuesta.PaginaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/mermas")
@RequiredArgsConstructor
@Tag(name = "Mermas", description = "Registro de mermas de inventario")
public class MermaController {

    private final MermaService mermaService;

    @PostMapping
    @Operation(summary = "Registrar merma", description = "Registra una merma manual sobre un contenedor")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Merma registrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Stock insuficiente")
    })
    public ResponseEntity<ApiResponse<MermaResponse>> registrar(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @Valid @RequestBody CrearMermaRequest request) {
        MermaResponse response = mermaService.registrar(empresaId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exitoso(response, "Merma registrada exitosamente"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener merma por ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Merma encontrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Merma no encontrada")
    })
    public ResponseEntity<ApiResponse<MermaResponse>> obtenerPorId(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PathVariable Long id) {
        MermaResponse response = mermaService.obtenerPorId(empresaId, id);
        return ResponseEntity.ok(ApiResponse.exitoso(response));
    }

    @GetMapping
    @Operation(summary = "Listar mermas por empresa", description = "Lista paginada de mermas registradas")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de mermas")
    })
    public ResponseEntity<ApiResponse<PaginaResponse<MermaResponse>>> listarPorEmpresa(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            Pageable pageable) {
        Page<MermaResponse> page = mermaService.listarPorEmpresa(empresaId, pageable);
        return ResponseEntity.ok(ApiResponse.exitoso(PaginaResponse.de(page)));
    }
}

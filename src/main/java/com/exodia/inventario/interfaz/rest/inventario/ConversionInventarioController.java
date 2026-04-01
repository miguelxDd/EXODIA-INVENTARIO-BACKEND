package com.exodia.inventario.interfaz.rest.inventario;

import com.exodia.inventario.aplicacion.comando.ConversionInventarioService;
import com.exodia.inventario.interfaz.dto.peticion.ConvertirInventarioRequest;
import com.exodia.inventario.interfaz.dto.respuesta.ApiResponse;
import com.exodia.inventario.interfaz.dto.respuesta.ConversionInventarioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventario/conversiones")
@RequiredArgsConstructor
@Tag(name = "Conversiones Inventario", description = "Conversion operativa de inventario por contenedor")
public class ConversionInventarioController {

    private final ConversionInventarioService conversionInventarioService;

    @PostMapping
    @Operation(summary = "Convertir inventario", description = "Convierte stock de un contenedor desde su unidad actual hacia otra unidad")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Conversion realizada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Conversion invalida"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Contenedor, unidad o conversion no encontrados")
    })
    public ResponseEntity<ApiResponse<ConversionInventarioResponse>> convertir(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @Valid @RequestBody ConvertirInventarioRequest request) {
        ConversionInventarioResponse response = conversionInventarioService.convertir(empresaId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exitoso(response, "Conversion de inventario realizada"));
    }
}

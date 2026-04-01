package com.exodia.inventario.interfaz.rest.inventario;

import com.exodia.inventario.aplicacion.consulta.StockQueryService;
import com.exodia.inventario.interfaz.dto.respuesta.ApiResponse;
import com.exodia.inventario.interfaz.dto.respuesta.ContenedorStockResponse;
import com.exodia.inventario.interfaz.dto.respuesta.PaginaResponse;
import com.exodia.inventario.interfaz.dto.respuesta.ProductoBodegaStockResponse;
import com.exodia.inventario.interfaz.mapeador.StockMapeador;
import com.exodia.inventario.repositorio.proyeccion.ContenedorStockProjection;
import com.exodia.inventario.repositorio.proyeccion.ProductoBodegaStockProjection;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

import static com.exodia.inventario.util.InventarioConstantes.MAX_RESULTADOS_PAGINA;
import static com.exodia.inventario.util.InventarioConstantes.TAMANIO_PAGINA_DEFAULT;

@RestController
@RequestMapping("/api/v1/inventario/stock")
@RequiredArgsConstructor
@Tag(name = "Stock", description = "Consultas de stock de inventario")
public class StockController {

    private final StockQueryService stockQueryService;
    private final StockMapeador stockMapeador;

    @GetMapping("/contenedor/{contenedorId}")
    @Operation(summary = "Stock de un contenedor", description = "Obtiene el stock total calculado de un contenedor")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Stock del contenedor"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Contenedor no encontrado")
    })
    public ResponseEntity<ApiResponse<BigDecimal>> stockPorContenedor(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PathVariable Long contenedorId) {
        BigDecimal stock = stockQueryService.obtenerStockContenedorPorEmpresa(empresaId, contenedorId);
        return ResponseEntity.ok(ApiResponse.exitoso(stock));
    }

    @GetMapping("/barcode/{codigoBarras}")
    @Operation(summary = "Stock por codigo de barras", description = "Obtiene el stock total por codigo de barras del contenedor")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Stock por barcode")
    })
    public ResponseEntity<ApiResponse<BigDecimal>> stockPorBarcode(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PathVariable String codigoBarras) {
        BigDecimal stock = stockQueryService.obtenerStockPorBarcode(empresaId, codigoBarras);
        return ResponseEntity.ok(ApiResponse.exitoso(stock));
    }

    @GetMapping("/producto-bodega")
    @Operation(summary = "Stock por producto y bodega", description = "Obtiene el stock total de un producto en una bodega")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Stock por producto y bodega")
    })
    public ResponseEntity<ApiResponse<BigDecimal>> stockPorProductoYBodega(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @RequestParam Long productoId,
            @RequestParam Long bodegaId) {
        BigDecimal stock = stockQueryService.obtenerStockPorProductoYBodega(empresaId, productoId, bodegaId);
        return ResponseEntity.ok(ApiResponse.exitoso(stock));
    }

    @GetMapping("/consolidado")
    @Operation(summary = "Stock consolidado", description = "Consulta consolidada de stock con filtros opcionales, paginada")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Stock consolidado paginado")
    })
    public ResponseEntity<ApiResponse<PaginaResponse<ContenedorStockResponse>>> stockConsolidado(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @RequestParam(required = false) Long bodegaId,
            @RequestParam(required = false) Long productoId,
            @RequestParam(required = false) Long proveedorId,
            @RequestParam(required = false) String codigoBarras,
            @RequestParam(required = false) String numeroLote,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "" + TAMANIO_PAGINA_DEFAULT) int tamanio) {
        Pageable pageable = PageRequest.of(pagina, Math.min(tamanio, MAX_RESULTADOS_PAGINA));
        Page<ContenedorStockProjection> page = stockQueryService.obtenerStockConsolidado(
                empresaId, bodegaId, productoId, proveedorId, codigoBarras, numeroLote, pageable);
        Page<ContenedorStockResponse> responsePage = page.map(stockMapeador::toResponse);
        return ResponseEntity.ok(ApiResponse.exitoso(PaginaResponse.de(responsePage)));
    }

    @GetMapping("/agrupado")
    @Operation(summary = "Stock agrupado por producto-bodega", description = "Obtiene stock agrupado por producto y bodega")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Stock agrupado")
    })
    public ResponseEntity<ApiResponse<List<ProductoBodegaStockResponse>>> stockAgrupado(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @RequestParam(required = false) Long bodegaId,
            @RequestParam(required = false) Long productoId) {
        List<ProductoBodegaStockProjection> projections =
                stockQueryService.obtenerStockPorProductoBodega(empresaId, bodegaId, productoId);
        List<ProductoBodegaStockResponse> response = stockMapeador.toProductoBodegaResponseList(projections);
        return ResponseEntity.ok(ApiResponse.exitoso(response));
    }

    @GetMapping("/proximos-a-vencer")
    @Operation(summary = "Contenedores proximos a vencer",
            description = "Contenedores con stock cuya fecha de vencimiento esta dentro del umbral configurado (diasAlertaVencimiento)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Contenedores proximos a vencer")
    })
    public ResponseEntity<ApiResponse<List<ContenedorStockResponse>>> proximosAVencer(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @RequestParam(required = false) Long bodegaId) {
        List<ContenedorStockProjection> projections =
                stockQueryService.obtenerContenedoresProximosAVencer(empresaId, bodegaId);
        List<ContenedorStockResponse> response = stockMapeador.toResponseList(projections);
        return ResponseEntity.ok(ApiResponse.exitoso(response));
    }

    @GetMapping("/disponible-fefo")
    @Operation(summary = "Contenedores disponibles FEFO", description = "Contenedores con stock disponible ordenados por fecha de vencimiento ASC")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Contenedores FEFO")
    })
    public ResponseEntity<ApiResponse<List<ContenedorStockResponse>>> disponiblesFEFO(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @RequestParam Long productoId,
            @RequestParam Long bodegaId) {
        List<ContenedorStockProjection> projections =
                stockQueryService.obtenerContenedoresDisponiblesFEFO(empresaId, productoId, bodegaId);
        List<ContenedorStockResponse> response = stockMapeador.toResponseList(projections);
        return ResponseEntity.ok(ApiResponse.exitoso(response));
    }
}

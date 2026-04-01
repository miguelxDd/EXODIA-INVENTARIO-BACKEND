package com.exodia.inventario.aplicacion.consulta.impl;

import com.exodia.inventario.aplicacion.consulta.EtiquetaInventarioService;
import com.exodia.inventario.aplicacion.consulta.StockQueryService;
import com.exodia.inventario.domain.modelo.catalogo.Ubicacion;
import com.exodia.inventario.domain.modelo.contenedor.Contenedor;
import com.exodia.inventario.excepcion.EntidadNoEncontradaException;
import com.exodia.inventario.interfaz.dto.respuesta.EtiquetaResponse;
import com.exodia.inventario.repositorio.catalogo.UbicacionRepository;
import com.exodia.inventario.repositorio.contenedor.ContenedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EtiquetaInventarioServiceImpl implements EtiquetaInventarioService {

    private final ContenedorRepository contenedorRepository;
    private final UbicacionRepository ubicacionRepository;
    private final StockQueryService stockQueryService;

    @Override
    public EtiquetaResponse generarEtiquetaContenedor(Long empresaId, Long contenedorId) {
        Contenedor contenedor = contenedorRepository.findById(contenedorId)
                .filter(c -> c.getEmpresa().getId().equals(empresaId))
                .filter(c -> Boolean.TRUE.equals(c.getActivo()))
                .orElseThrow(() -> new EntidadNoEncontradaException("Contenedor", contenedorId));

        String codigoBarras = contenedor.getCodigoBarras();
        BigDecimal stock = stockQueryService.obtenerStockContenedorPorEmpresa(empresaId, contenedorId);

        List<String> detalles = new ArrayList<>();
        detalles.add("Producto: " + contenedor.getProductoId());
        detalles.add("Bodega: " + contenedor.getBodega().getCodigo());
        if (contenedor.getUbicacion() != null) {
            detalles.add("Ubicacion: " + contenedor.getUbicacion().getCodigo());
        }
        detalles.add("Unidad: " + contenedor.getUnidad().getAbreviatura());
        detalles.add("Stock: " + formatearDecimal(stock));
        if (contenedor.getNumeroLote() != null && !contenedor.getNumeroLote().isBlank()) {
            detalles.add("Lote: " + contenedor.getNumeroLote());
        }
        if (contenedor.getFechaVencimiento() != null) {
            detalles.add("Vence: " + contenedor.getFechaVencimiento());
        }

        return construirEtiqueta(
                "CONTENEDOR",
                contenedor.getId(),
                codigoBarras,
                codigoBarras,
                "Prod " + contenedor.getProductoId(),
                detalles);
    }

    @Override
    public EtiquetaResponse generarEtiquetaUbicacion(Long empresaId, Long ubicacionId) {
        Ubicacion ubicacion = ubicacionRepository.findById(ubicacionId)
                .filter(u -> u.getBodega().getEmpresa().getId().equals(empresaId))
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .orElseThrow(() -> new EntidadNoEncontradaException("Ubicacion", ubicacionId));

        String codigoBarras = ubicacion.getCodigoBarras() != null && !ubicacion.getCodigoBarras().isBlank()
                ? ubicacion.getCodigoBarras()
                : ubicacion.getCodigo();

        List<String> detalles = List.of(
                "Bodega: " + ubicacion.getBodega().getCodigo(),
                "Nombre: " + ubicacion.getNombre(),
                "Tipo: " + ubicacion.getTipoUbicacion().name());

        return construirEtiqueta(
                "UBICACION",
                ubicacion.getId(),
                codigoBarras,
                ubicacion.getCodigo(),
                ubicacion.getNombre(),
                detalles);
    }

    private EtiquetaResponse construirEtiqueta(String tipoEtiqueta,
                                               Long entidadId,
                                               String codigoBarras,
                                               String titulo,
                                               String subtitulo,
                                               List<String> detalles) {
        return new EtiquetaResponse(
                tipoEtiqueta,
                entidadId,
                codigoBarras,
                titulo,
                subtitulo,
                List.copyOf(detalles),
                construirZpl(titulo, subtitulo, codigoBarras, detalles),
                construirSvgVistaPrevia(titulo, subtitulo, codigoBarras, detalles));
    }

    private String construirZpl(String titulo,
                                String subtitulo,
                                String codigoBarras,
                                List<String> detalles) {
        StringBuilder zpl = new StringBuilder();
        zpl.append("^XA\n")
                .append("^PW800\n")
                .append("^LL600\n")
                .append("^CF0,34\n")
                .append("^FO40,30^FD").append(escapeZpl(titulo)).append("^FS\n")
                .append("^CF0,24\n")
                .append("^FO40,75^FD").append(escapeZpl(subtitulo)).append("^FS\n")
                .append("^BY2,3,90\n")
                .append("^FO40,120^BCN,100,Y,N,N\n")
                .append("^FD").append(escapeZpl(codigoBarras)).append("^FS\n");

        int y = 250;
        for (String detalle : detalles) {
            zpl.append("^FO40,").append(y).append("^FD")
                    .append(escapeZpl(detalle))
                    .append("^FS\n");
            y += 30;
        }

        zpl.append("^XZ");
        return zpl.toString();
    }

    private String construirSvgVistaPrevia(String titulo,
                                           String subtitulo,
                                           String codigoBarras,
                                           List<String> detalles) {
        int altoBase = 270 + (detalles.size() * 24);
        StringBuilder svg = new StringBuilder();
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"600\" height=\"")
                .append(altoBase)
                .append("\" viewBox=\"0 0 600 ")
                .append(altoBase)
                .append("\">");
        svg.append("<rect x=\"1\" y=\"1\" width=\"598\" height=\"")
                .append(altoBase - 2)
                .append("\" fill=\"#fffdf8\" stroke=\"#1f2937\" stroke-width=\"2\"/>");
        svg.append("<text x=\"24\" y=\"42\" font-family=\"monospace\" font-size=\"24\" font-weight=\"700\">")
                .append(escapeXml(titulo))
                .append("</text>");
        svg.append("<text x=\"24\" y=\"72\" font-family=\"monospace\" font-size=\"18\">")
                .append(escapeXml(subtitulo))
                .append("</text>");
        svg.append("<rect x=\"24\" y=\"95\" width=\"552\" height=\"80\" fill=\"#f3f4f6\" stroke=\"#111827\" stroke-width=\"1\"/>");
        svg.append("<text x=\"36\" y=\"143\" font-family=\"monospace\" font-size=\"26\" letter-spacing=\"2\">")
                .append(escapeXml(codigoBarras))
                .append("</text>");

        int y = 210;
        for (String detalle : detalles) {
            svg.append("<text x=\"24\" y=\"").append(y)
                    .append("\" font-family=\"monospace\" font-size=\"16\">")
                    .append(escapeXml(detalle))
                    .append("</text>");
            y += 24;
        }

        svg.append("</svg>");
        return svg.toString();
    }

    private String escapeZpl(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replace("^", " ").replace("~", " ");
    }

    private String escapeXml(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private String formatearDecimal(BigDecimal valor) {
        if (valor == null) {
            return "0";
        }
        return valor.stripTrailingZeros().toPlainString();
    }
}

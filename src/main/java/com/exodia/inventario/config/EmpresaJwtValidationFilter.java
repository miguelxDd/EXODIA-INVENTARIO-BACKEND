package com.exodia.inventario.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Endurece multiempresa en prod: si el JWT trae empresa_id o tenant_id,
 * el header X-Empresa-Id debe coincidir.
 */
@Component
@Profile("prod")
public class EmpresaJwtValidationFilter extends OncePerRequestFilter {

    private static final String HEADER_EMPRESA = "X-Empresa-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        HttpServletRequest requestProcesado = request;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            Jwt jwt = jwtAuthenticationToken.getToken();
            String empresaHeader = request.getHeader(HEADER_EMPRESA);
            String empresaClaim = extraerEmpresa(jwt);

            if (empresaHeader != null && empresaClaim != null && !empresaHeader.equals(empresaClaim)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "X-Empresa-Id no coincide con el tenant autenticado");
                return;
            }

            if ((empresaHeader == null || empresaHeader.isBlank())
                    && empresaClaim != null && !empresaClaim.isBlank()) {
                requestProcesado = new HeaderOverrideRequestWrapper(request, HEADER_EMPRESA, empresaClaim);
            }
        }

        filterChain.doFilter(requestProcesado, response);
    }

    private String extraerEmpresa(Jwt jwt) {
        Object empresaId = jwt.getClaims().get("empresa_id");
        if (empresaId == null) {
            empresaId = jwt.getClaims().get("tenant_id");
        }
        return empresaId != null ? String.valueOf(empresaId) : null;
    }

    private static final class HeaderOverrideRequestWrapper extends HttpServletRequestWrapper {

        private final Map<String, String> headers = new LinkedHashMap<>();

        private HeaderOverrideRequestWrapper(HttpServletRequest request,
                                             String headerName,
                                             String headerValue) {
            super(request);
            headers.put(headerName, headerValue);
        }

        @Override
        public String getHeader(String name) {
            String value = headers.get(name);
            return value != null ? value : super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            String value = headers.get(name);
            if (value != null) {
                return Collections.enumeration(List.of(value));
            }
            return super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            var names = new java.util.LinkedHashSet<String>();
            Enumeration<String> existing = super.getHeaderNames();
            while (existing.hasMoreElements()) {
                names.add(existing.nextElement());
            }
            names.addAll(headers.keySet());
            return Collections.enumeration(names);
        }
    }
}

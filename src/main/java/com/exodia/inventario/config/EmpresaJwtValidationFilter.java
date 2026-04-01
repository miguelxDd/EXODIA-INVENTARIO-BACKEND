package com.exodia.inventario.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

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
        }

        filterChain.doFilter(request, response);
    }

    private String extraerEmpresa(Jwt jwt) {
        Object empresaId = jwt.getClaims().get("empresa_id");
        if (empresaId == null) {
            empresaId = jwt.getClaims().get("tenant_id");
        }
        return empresaId != null ? String.valueOf(empresaId) : null;
    }
}

package com.exodia.inventario.unit.config;

import com.exodia.inventario.config.EmpresaJwtValidationFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@Tag("unit")
class EmpresaJwtValidationFilterTest {

    private final EmpresaJwtValidationFilter filter = new EmpresaJwtValidationFilter();

    @Test
    void deberiaInyectarHeaderEmpresaDesdeJwtSiNoLlegaEnRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        SecurityContextHolder.getContext().setAuthentication(jwtAuth("15"));

        filter.doFilter(request, response, chain);

        HttpServletRequest requestProcesado = (HttpServletRequest) chain.getRequest();
        assertEquals("15", requestProcesado.getHeader("X-Empresa-Id"));
        SecurityContextHolder.clearContext();
    }

    @Test
    void deberiaRechazarSiHeaderNoCoincideConJwt() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Empresa-Id", "99");
        MockHttpServletResponse response = new MockHttpServletResponse();

        SecurityContextHolder.getContext().setAuthentication(jwtAuth("15"));

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(403, response.getStatus());
        assertNull(response.getHeader("X-Empresa-Id"));
        SecurityContextHolder.clearContext();
    }

    private JwtAuthenticationToken jwtAuth(String empresaId) {
        Jwt jwt = new Jwt(
                "token",
                java.time.Instant.now(),
                java.time.Instant.now().plusSeconds(3600),
                Map.of("alg", "none"),
                Map.of("empresa_id", empresaId, "sub", "1"));
        return new JwtAuthenticationToken(jwt);
    }
}

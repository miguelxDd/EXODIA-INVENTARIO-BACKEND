package com.exodia.inventario.unit.config;

import com.exodia.inventario.config.CorrelationIdFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@Tag("unit")
class CorrelationIdFilterTest {

    private final CorrelationIdFilter correlationIdFilter = new CorrelationIdFilter();

    @AfterEach
    void limpiarMdc() {
        MDC.clear();
    }

    @Test
    void deberiaReutilizarCorrelationIdEnviadoPorCliente() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Correlation-Id", "corr-cliente-001");
        MockHttpServletResponse response = new MockHttpServletResponse();

        correlationIdFilter.doFilter(request, response, new MockFilterChain());

        assertEquals("corr-cliente-001", response.getHeader("X-Correlation-Id"));
        assertNull(MDC.get("correlationId"));
    }

    @Test
    void deberiaGenerarCorrelationIdSiNoLlegaEnRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        correlationIdFilter.doFilter(request, response, new MockFilterChain());

        String correlationId = response.getHeader("X-Correlation-Id");
        assertNotNull(correlationId);
        assertNull(MDC.get("correlationId"));
    }
}

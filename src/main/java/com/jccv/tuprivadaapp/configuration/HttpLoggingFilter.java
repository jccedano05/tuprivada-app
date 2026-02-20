package com.jccv.tuprivadaapp.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro simple para trazar todas las peticiones HTTP y saber si llegan al stack de Spring.
 * Nos ayuda a diagnosticar respuestas 4xx/5xx antes de que alcancen los controladores.
 */
@Component
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class HttpLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        log.info("[HTTP] {} {}{} from {}", method, uri,
                (query != null ? "?" + query : ""), request.getRemoteAddr());

        try {
            filterChain.doFilter(request, response);
        } finally {
            stopWatch.stop();
            log.info("[HTTP] {} {} -> {} ({} ms)", method, uri, response.getStatus(), stopWatch.getTotalTimeMillis());
        }
    }
}

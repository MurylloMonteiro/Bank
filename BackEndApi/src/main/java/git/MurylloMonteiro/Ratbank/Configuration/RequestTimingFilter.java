package git.MurylloMonteiro.Ratbank.Configuration;

import java.io.IOException;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RequestTimingFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(RequestTimingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        long startedAt = System.nanoTime();
        try {
            chain.doFilter(request, response);
        } finally {
            double elapsedMs = (System.nanoTime() - startedAt) / 1_000_000.0;
            response.setHeader("Server-Timing", String.format(Locale.ROOT, "app;dur=%.1f", elapsedMs));
            if (elapsedMs >= 500) {
                log.warn("{} {} respondeu com status {} em {} ms", request.getMethod(),
                        request.getRequestURI(), response.getStatus(), Math.round(elapsedMs));
            }
        }
    }
}

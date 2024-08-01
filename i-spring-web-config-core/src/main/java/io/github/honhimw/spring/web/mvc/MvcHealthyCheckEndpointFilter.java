package io.github.honhimw.spring.web.mvc;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.PathContainer;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 健康检查
 * @author hon_him
 * @since 2023-05-19
 */

@Slf4j
public class MvcHealthyCheckEndpointFilter extends OncePerRequestFilter {

    private final List<PathPattern> _pathPatterns = new ArrayList<>();

    public MvcHealthyCheckEndpointFilter() {
        this("/healthy");
    }

    public MvcHealthyCheckEndpointFilter(String... paths) {
        for (String path : paths) {
            _pathPatterns.add(PathPatternParser.defaultInstance.parse(path));
        }
    }

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull FilterChain filterChain) throws ServletException, IOException {
        String servletPath = request.getServletPath();
        if (_pathPatterns.stream().anyMatch(pathPattern -> pathPattern.matches(PathContainer.parsePath(servletPath)))) {
            response.setStatus(HttpStatus.OK.value());
            response.getWriter().write("OK");
            return;
        }
        filterChain.doFilter(request, response);
    }

}

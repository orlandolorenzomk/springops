package org.kreyzon.springops.auth.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.kreyzon.springops.auth.service.UserService;
import org.kreyzon.springops.auth.util.JwtUtil;
import org.kreyzon.springops.config.JwtConfig;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter for handling JWT authentication in incoming HTTP requests.
 * Validates the JWT token, extracts the username, and sets the authentication
 * in the security context if the token is valid.
 * <p>
 * This filter is executed once per request and uses {@link JwtUtil} for token operations
 * and {@link UserService} for loading user details.
 * <p>
 * Configuration properties are sourced from {@link JwtConfig}.
 *
 * @author Lorenzo Orlando
 * @email orlandolorenzo@kreyzon.com
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserService userDetailsService;
    private final JwtConfig jwtConfig;

    /**
     * Processes incoming HTTP requests to validate JWT tokens.
     * If a valid token is found, the user is authenticated and the security context is updated.
     *
     * @param request  the HTTP request.
     * @param response the HTTP response.
     * @param filterChain the filter chain to pass the request and response to the next filter.
     * @throws ServletException if an error occurs during request processing.
     * @throws IOException if an I/O error occurs during request processing.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader(jwtConfig.getHeader());

        if (authHeader == null || !authHeader.startsWith(jwtConfig.getPrefix())) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            String username = jwtUtil.extractUsername(token);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtUtil.validateToken(token)) {
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }

            filterChain.doFilter(request, response);
        } catch (io.jsonwebtoken.ExpiredJwtException ex) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("""
            {
              "error": "Your session has expired. Please log in again.",
              "code": "JWT_EXPIRED"
            }
        """);
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("""
            {
              "error": "Invalid or malformed token.",
              "code": "JWT_INVALID"
            }
        """);
        }
    }
}
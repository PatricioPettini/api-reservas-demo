package com.patojunit.security.config.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.patojunit.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

//mediante el extends establecemos que es un filtro que se tiene que ejecutar siempre
public class JwtTokenValidator extends OncePerRequestFilter {


    private JwtUtils jwtUtils;

    public JwtTokenValidator(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String jwtToken = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (jwtToken != null && jwtToken.startsWith("Bearer ")) {
            // Extraer solo el token después del prefijo
            jwtToken = jwtToken.substring(7);

            DecodedJWT decodedJWT = jwtUtils.validateToken(jwtToken);
            String username = jwtUtils.extractUsername(decodedJWT);

            var claim = jwtUtils.getSpecificClaim(decodedJWT, "authorities");
            String authorities = (claim != null) ? claim.asString() : "";

            var authoritiesList = AuthorityUtils.commaSeparatedStringToAuthorityList(authorities);

            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(username, null, authoritiesList);

            SecurityContext context = SecurityContextHolder.getContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
        }

        // Continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }

}

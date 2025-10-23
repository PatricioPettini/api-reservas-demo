package com.patojunit.helpers.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class JwtUserProvider {

    public String getUsuarioAutenticadoUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getPrincipal() == null) {
            throw new SecurityException("No hay usuario autenticado.");
        }

        return auth.getName();
    }

}

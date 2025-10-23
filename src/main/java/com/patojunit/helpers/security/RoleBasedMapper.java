package com.patojunit.helpers.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class RoleBasedMapper {

    private final JwtRoleValidator jwtRoleValidator;

    public <T, R> R mapByRole(
            T entity,
            Function<T, R> userMapper,
            Function<T, ? extends R> adminMapper) {

        return jwtRoleValidator.isAdmin()
                ? adminMapper.apply(entity)
                : userMapper.apply(entity);
    }
}

package com.patojunit.service.interfaces;

import com.patojunit.model.Role;
import java.util.Optional;

import java.util.List;

public interface IRoleService {
    List<Role> findAll();
    Optional<Role> findById(Long id);
    Role save(Role role);
    void deleteById(Long id);
    Role update(Role role);
}

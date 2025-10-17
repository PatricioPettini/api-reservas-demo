package com.patojunit.controller;

import com.patojunit.model.Role;
import com.patojunit.model.UserSec;
import com.patojunit.service.IRoleService;
import com.patojunit.service.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Mock
    private IUserService userService;

    @Mock
    private IRoleService roleService;

    @InjectMocks
    private UserController userController;

    private UserSec user1;
    private UserSec user2;
    private Role roleAdmin;
    private Role roleUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        roleAdmin = new Role(1L, "ADMIN", Set.of());
        roleUser = new Role(2L, "USER", Set.of());

        user1 = new UserSec(1L, "juan", "1234", true, true, true, true, Set.of(roleAdmin));
        user2 = new UserSec(2L, "maria", "abcd", true, true, true, true, Set.of(roleUser));
    }

    @Test
    void getAllUsers_DeberiaRetornarListaYStatusOk() {
        when(userService.findAll()).thenReturn(Arrays.asList(user1, user2));

        ResponseEntity<List<UserSec>> response = userController.getAllUsers();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(2, response.getBody().size());
        verify(userService, times(1)).findAll();
    }

    @Test
    void getUserById_DeberiaRetornarOkSiExiste() {
        when(userService.findById(1L)).thenReturn(Optional.of(user1));

        ResponseEntity<UserSec> response = userController.getUserById(1L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("juan", response.getBody().getUsername());
        verify(userService, times(1)).findById(1L);
    }

    @Test
    void getUserById_DeberiaRetornarNotFoundSiNoExiste() {
        when(userService.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<UserSec> response = userController.getUserById(99L);

        assertEquals(404, response.getStatusCode().value());
        assertNull(response.getBody());
        verify(userService, times(1)).findById(99L);
    }

    @Test
    void createUser_DeberiaGuardarUsuarioConRoles() {
        // Arrange
        UserSec newUser = new UserSec(null, "lucas", "pass", true, true, true, true, Set.of(roleAdmin));
        UserSec savedUser = new UserSec(3L, "lucas", "hashedPass", true, true, true, true, Set.of(roleAdmin));

        when(userService.encriptPassword("pass")).thenReturn("hashedPass");
        when(roleService.findById(1L)).thenReturn(Optional.of(roleAdmin));
        when(userService.save(any(UserSec.class))).thenReturn(savedUser);

        // Act
        ResponseEntity<UserSec> response = userController.createUser(newUser);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals("lucas", response.getBody().getUsername());
        assertEquals("hashedPass", response.getBody().getPassword());
        verify(userService, times(1)).encriptPassword("pass");
        verify(userService, times(1)).save(any(UserSec.class));
    }

    @Test
    void createUser_DeberiaRetornarNullSiNoHayRolesValidos() {
        UserSec newUser = new UserSec(null, "pepe", "pass", true, true, true, true, Set.of(roleAdmin));
        when(userService.encriptPassword("pass")).thenReturn("hashedPass");
        when(roleService.findById(anyLong())).thenReturn(Optional.empty());

        ResponseEntity<UserSec> response = userController.createUser(newUser);

        assertNull(response);
        verify(userService, never()).save(any());
    }
}

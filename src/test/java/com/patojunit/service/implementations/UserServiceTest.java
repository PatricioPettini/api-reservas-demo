package com.patojunit.service.implementations;

import com.patojunit.model.UserSec;
import com.patojunit.repository.IUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private IUserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UserSec user1;
    private UserSec user2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user1 = new UserSec(1L, "juan", "1234", true, true, true, true, Set.of(), null);
        user2 = new UserSec(2L, "maria", "abcd", true, true, true, true, Set.of(), null);
    }

    @Test
    void findAll_DeberiaRetornarListaDeUsuarios() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        List<UserSec> result = userService.findAll();

        assertEquals(2, result.size());
        assertEquals("juan", result.get(0).getUsername());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void findById_DeberiaRetornarOptionalConUsuario() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));

        Optional<UserSec> result = userService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("juan", result.get().getUsername());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void findById_DeberiaRetornarOptionalVacioSiNoExiste() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<UserSec> result = userService.findById(99L);

        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findById(99L);
    }

    @Test
    void save_DeberiaGuardarYRetornarUsuario() {
        UserSec newUser = new UserSec(null, "lucas", "pass", true, true, true, true, Set.of(), null);
        UserSec savedUser = new UserSec(3L, "lucas", "pass", true, true, true, true, Set.of(), null);

        when(userRepository.save(newUser)).thenReturn(savedUser);

        UserSec result = userService.save(newUser);

        assertEquals(3L, result.getId());
        assertEquals("lucas", result.getUsername());
        verify(userRepository, times(1)).save(newUser);
    }

    @Test
    void deleteById_DeberiaLlamarAlRepositorio() {
        userService.deleteById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void update_DeberiaGuardarUsuarioActualizado() {
        UserSec updated = new UserSec(1L, "juan_updated", "9999", true, true, true, true, Set.of(), null);
        when(userRepository.save(updated)).thenReturn(updated);

        userService.update(updated);

        verify(userRepository, times(1)).save(updated);
    }

    @Test
    void encriptPassword_DeberiaEncriptarCorrectamente() {
        String rawPassword = "mypassword";

        String encodedPassword = userService.encriptPassword(rawPassword);

        assertNotNull(encodedPassword);
        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(new BCryptPasswordEncoder().matches(rawPassword, encodedPassword),
                "La contraseña debería ser válida al comparar con el hash generado");
    }
}

package com.jahid.minimarketplace.service;

import com.jahid.minimarketplace.dto.RegisterRequest;
import com.jahid.minimarketplace.dto.UserDTO;
import com.jahid.minimarketplace.entity.Role;
import com.jahid.minimarketplace.entity.User;
import com.jahid.minimarketplace.exception.DuplicateResourceException;
import com.jahid.minimarketplace.exception.ResourceNotFoundException;
import com.jahid.minimarketplace.repository.RoleRepository;
import com.jahid.minimarketplace.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;
    private Role buyerRole;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        buyerRole = Role.builder().id(1L).name(Role.RoleName.ROLE_BUYER).build();

        user = User.builder()
                .id(1L)
                .username("testUser")
                .email("test@example.com")
                .password("encodedPassword")
                .fullName("Test User")
                .enabled(true)
                .roles(Set.of(buyerRole))
                .build();

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newUser");
        registerRequest.setEmail("new@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFullName("New User");
        registerRequest.setRole("BUYER");
    }

    // UT-1: Successful registration
    @Test
    void register_Success() {
        when(userRepository.existsByUsername("newUser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(roleRepository.findByName(Role.RoleName.ROLE_BUYER)).thenReturn(Optional.of(buyerRole));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDTO result = userService.register(registerRequest);

        assertNotNull(result);
        assertEquals("testUser", result.getUsername());
        verify(userRepository).save(any(User.class));
    }

    // UT-2: Duplicate username rejected
    @Test
    void register_DuplicateUsername_ThrowsException() {
        when(userRepository.existsByUsername("newUser")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    // UT-3: Get user by ID — success
    @Test
    void getUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDTO result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals("testUser", result.getUsername());
    }

    // UT-4: Get user by ID — not found
    @Test
    void getUserById_NotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(99L));
    }

    // UT-5: Toggle user enabled/disabled
    @Test
    void toggleUserEnabled_DisablesUser() {
        user.setEnabled(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.toggleUserEnabled(1L);

        assertFalse(user.isEnabled());
        verify(userRepository).save(user);
    }
}

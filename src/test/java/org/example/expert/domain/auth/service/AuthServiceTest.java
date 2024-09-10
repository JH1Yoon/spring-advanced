package org.example.expert.domain.auth.service;

import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSignupWithNullEmailThrowsException() {
        SignupRequest request = new SignupRequest(null, "password", "USER_ROLE");

        InvalidRequestException thrownException = assertThrows(InvalidRequestException.class, () -> {
            authService.signup(request);
        });

        assertEquals("이메일이 필요합니다.", thrownException.getMessage());
    }

    @Test
    void testSignupWithEmptyEmailThrowsException() {
        SignupRequest request = new SignupRequest("", "password", "USER_ROLE");

        InvalidRequestException thrownException = assertThrows(InvalidRequestException.class, () -> {
            authService.signup(request);
        });

        assertEquals("이메일이 필요합니다.", thrownException.getMessage());
    }

    @Test
    void testSignupWithExistingEmailThrowsException() {
        SignupRequest request = new SignupRequest("existing@example.com", "password", "USER_ROLE");
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        InvalidRequestException thrownException = assertThrows(InvalidRequestException.class, () -> {
            authService.signup(request);
        });

        assertEquals("이미 존재하는 이메일입니다.", thrownException.getMessage());
    }
}
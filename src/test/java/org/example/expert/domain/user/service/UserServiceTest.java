package org.example.expert.domain.user.service;

import org.example.expert.config.PasswordEncoder;
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

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void 비밀번호가_짧을_때_예외가_발생한다() {
        String shortPassword = "short";

        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            userService.validateNewPassword(shortPassword);
        });

        assertEquals("새 비밀번호는 8자 이상이어야 하고, 숫자와 대문자를 포함해야 합니다.", exception.getMessage());
    }

    @Test
    public void 비밀번호에_숫자가_포함되지_않을_때_예외가_발생한다() {
        String noNumberPassword = "Password";

        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            userService.validateNewPassword(noNumberPassword);
        });

        assertEquals("새 비밀번호는 8자 이상이어야 하고, 숫자와 대문자를 포함해야 합니다.", exception.getMessage());
    }

    @Test
    public void 비밀번호에_대문자가_포함되지_않을_때_예외가_발생한다() {
        String noUppercasePassword = "password1";

        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            userService.validateNewPassword(noUppercasePassword);
        });

        assertEquals("새 비밀번호는 8자 이상이어야 하고, 숫자와 대문자를 포함해야 합니다.", exception.getMessage());
    }

    @Test
    public void 유효한_비밀번호일_때_예외가_발생하지_않는다() {
        String validPassword = "Valid1Password";

        assertDoesNotThrow(() -> userService.validateNewPassword(validPassword));
    }
}

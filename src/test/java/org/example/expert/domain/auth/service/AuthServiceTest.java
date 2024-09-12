package org.example.expert.domain.auth.service;

import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

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

    @Test
    void 회원가입시_이메일이_Null일_때_예외를_던진다() {
        // given
        SignupRequest request = new SignupRequest(null, "password", "USER_ROLE");

        // when & then
        InvalidRequestException thrownException = assertThrows(InvalidRequestException.class, () -> {
            authService.signup(request);
        });

        assertEquals("이메일이 필요합니다.", thrownException.getMessage());
    }

    @Test
    void 회원가입시_이메일이_빈문자일_때_예외를_던진다() {
        // given
        SignupRequest request = new SignupRequest("", "password", "USER_ROLE");

        // when & then
        InvalidRequestException thrownException = assertThrows(InvalidRequestException.class, () -> {
            authService.signup(request);
        });

        assertEquals("이메일이 필요합니다.", thrownException.getMessage());
    }

    @Test
    void 회원가입시_이미_존재하는_이메일일_때_예외를_던진다() {
        // given
        SignupRequest request = new SignupRequest("existing@example.com", "password", "USER");

        given(userRepository.existsByEmail(request.getEmail())).willReturn(true);

        // when & then
        InvalidRequestException thrownException = assertThrows(InvalidRequestException.class, () -> {
            authService.signup(request);
        });

        assertEquals("이미 존재하는 이메일입니다.", thrownException.getMessage());
    }

    @Test
    void 회원가입시_성공적으로_회원가입이_될_때_회원정보가_저장되고_토큰이_반환된다() {
        // given
        SignupRequest request = new SignupRequest("newuser@example.com", "password", "USER");

        User newUser = new User("newuser@example.com", "encodedPassword", UserRole.USER);
        User savedUser = new User("newuser@example.com", "encodedPassword", UserRole.USER);
        String token = "jwtToken";

        given(userRepository.existsByEmail("newuser@example.com")).willReturn(false);  // 이메일이 존재하지 않음
        given(passwordEncoder.encode("password")).willReturn("encodedPassword");  // 비밀번호 인코딩
        given(userRepository.save(any(User.class))).willReturn(savedUser);  // 사용자 저장
        given(jwtUtil.createToken(savedUser.getId(), savedUser.getEmail(), UserRole.USER)).willReturn(token);  // 토큰 생성

        // when
        SignupResponse response = authService.signup(request);

        // then
        assertEquals(token, response.getBearerToken());
        verify(userRepository).save(any(User.class));  // userRepository.save가 호출되었는지 확인
    }
}
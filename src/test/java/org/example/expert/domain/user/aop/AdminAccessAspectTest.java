package org.example.expert.domain.user.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import org.example.expert.config.JwtUtil;
import org.example.expert.domain.comment.controller.CommentAdminController;
import org.example.expert.domain.comment.service.CommentAdminService;
import org.example.expert.domain.user.config.AspectConfig;
import org.example.expert.domain.user.controller.UserAdminController;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.service.UserAdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.Mockito.*;

@WebMvcTest(controllers = {CommentAdminController.class, UserAdminController.class})
@Import(AspectConfig.class) // Import AspectConfig for AOP
class AdminAccessAspectTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentAdminService commentAdminService;

    @MockBean
    private UserAdminService userAdminService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private String validToken;
    private String invalidToken;

    @BeforeEach
    void setUp() {
        validToken = jwtUtil.createToken(1L, "example@example.com", UserRole.ADMIN);
        invalidToken = jwtUtil.createToken(1L, "example@example.com", UserRole.USER);

        Claims validClaims = jwtUtil.extractClaims(validToken);
        Claims invalidClaims = jwtUtil.extractClaims(invalidToken);

        when(jwtUtil.extractClaims(validToken)).thenReturn(validClaims);
        when(jwtUtil.extractClaims(invalidToken)).thenReturn(invalidClaims);
    }

    @Test
    void comment를_정상적으로_삭제한다() throws Exception {
        // Given
        String endpoint = "/admin/comments/1";
        String authHeader = "Bearer " + validToken;

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.delete(endpoint)
                        .header(HttpHeaders.AUTHORIZATION, authHeader)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void comment를_잘못된_토큰으로_삭제한다() throws Exception {
        // Given
        String endpoint = "/admin/comments/1";
        String authHeader = "Bearer " + invalidToken;

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.delete(endpoint)
                        .header(HttpHeaders.AUTHORIZATION, authHeader)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    void userRole을_정상적으로_변경한다() throws Exception {
        // Given
        UserRoleChangeRequest request = new UserRoleChangeRequest(UserRole.USER.name());
        String endpoint = "/admin/users/1";
        String authHeader = "Bearer " + validToken;

        // When
        mockMvc.perform(MockMvcRequestBuilders.patch(endpoint)
                        .header(HttpHeaders.AUTHORIZATION, authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk());

        // Then
        verify(userAdminService, times(1)).changeUserRole(eq(1L), argThat(arg ->
                UserRole.valueOf(arg.getRole()).equals(UserRole.USER)));
    }

    @Test
    void userRole을_잘못된_토큰으로_변경한다() throws Exception {
        // Given
        UserRoleChangeRequest invalidRequest = new UserRoleChangeRequest(UserRole.ADMIN.name());
        String endpoint = "/admin/users/1";
        String authHeader = "Bearer " + invalidToken;

        // When
        mockMvc.perform(MockMvcRequestBuilders.patch(endpoint)
                        .header(HttpHeaders.AUTHORIZATION, authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(MockMvcResultMatchers.status().isForbidden());

        // Then
        verify(userAdminService, times(0)).changeUserRole(eq(1L), argThat(arg ->
                UserRole.valueOf(arg.getRole()).equals(UserRole.ADMIN)));
    }
}
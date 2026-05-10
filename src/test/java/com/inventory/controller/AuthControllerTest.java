package com.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.config.SecurityConfig;
import com.inventory.dto.auth.AuthRequest;
import com.inventory.dto.auth.AuthResponse;
import com.inventory.dto.auth.RegisterRequest;
import com.inventory.security.JwtService;
import com.inventory.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ── Controller dependency ─────────────────────────────────────────────────
    @MockBean
    private AuthService authService;

    // ── SecurityConfig needs AuthenticationProvider ───────────────────────────
    // ApplicationConfig (which defines this bean) is not loaded by @WebMvcTest.
    @MockBean
    private AuthenticationProvider authenticationProvider;

    // ── JwtAuthFilter (@Component Filter) needs JwtService + UserDetailsService
    // @WebMvcTest loads Filter beans automatically; their deps must be mocked.
    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    // ── Tests ─────────────────────────────────────────────────────────────────
    // No .with(csrf()) needed — SecurityConfig disables CSRF.
    // No authentication needed — /api/auth/** is permitAll().

    @Test
    void register_returnsCreatedWithToken() throws Exception {
        when(authService.register(any())).thenReturn(new AuthResponse("mocked-jwt-token"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("alice", "alice@example.com", "password123"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("mocked-jwt-token"));
    }

    @Test
    void register_withInvalidEmail_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("bob", "not-an-email", "password123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    @Test
    void login_returnsOkWithToken() throws Exception {
        when(authService.login(any())).thenReturn(new AuthResponse("mocked-jwt-token"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AuthRequest("alice", "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked-jwt-token"));
    }

    @Test
    void login_withMissingFields_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}

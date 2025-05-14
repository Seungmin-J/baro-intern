package com.example.intern_assignment.auth.service;

import com.example.intern_assignment.auth.dto.SignInRequest;
import com.example.intern_assignment.auth.dto.SignUpRequest;
import com.example.intern_assignment.auth.dto.SignUpResponse;
import com.example.intern_assignment.auth.dto.TokenResponse;
import com.example.intern_assignment.auth.exception.AuthException;
import com.example.intern_assignment.auth.jwt.JwtUtil;
import com.example.intern_assignment.user.entity.User;
import com.example.intern_assignment.user.enums.UserRole;
import com.example.intern_assignment.user.repository.MemoryUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private MemoryUserRepository userRepository = new MemoryUserRepository();
    private PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private JwtUtil jwtUtil = mock(JwtUtil.class);
    private AuthService authService = new AuthService(userRepository, passwordEncoder, jwtUtil);

    @Test
    void 회원가입_정상_케이스() {
        // given
        SignUpRequest request = SignUpRequest.builder()
                .username("testuser")
                .nickname("nickname")
                .password("password123")
                .build();

        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        // when
        SignUpResponse response = authService.signUp(request);

        // then
        Optional<User> saved = userRepository.findByUsername("testuser");
        assertTrue(saved.isPresent());
        User savedUser = saved.get();

        assertNotNull(savedUser.getId());
        assertEquals("testuser", savedUser.getUsername());
        assertEquals("nickname", savedUser.getNickname());
        assertEquals(Set.of(UserRole.USER), savedUser.getUserRoles());

        assertEquals("testuser", response.getUsername());
        assertEquals("nickname", response.getNickname());
    }

    @Test
    void 로그인_정상_케이스() {
        // given
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .password("encodedPassword")
                .userRoles(Set.of(UserRole.USER))
                .build();

        userRepository.save(user);

        SignInRequest request = SignInRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtUtil.createAccessToken(anyLong(), eq("testuser"), eq(Set.of(UserRole.USER))))
                .thenReturn("mocked.jwt.token");

        // when
        TokenResponse response = authService.signIn(request);

        // then
        assertEquals("mocked.jwt.token", response.getToken());
    }

    @Test
    void 관리자_권한_부여_정상() {
        // given
        User user = User.builder()
                .username("testuser")
                .nickname("nickname")
                .password("encodedPassword")
                .userRoles(new HashSet<>(Set.of(UserRole.USER)))
                .build();

        user = userRepository.save(user);

        // when
        SignUpResponse response = authService.grantAdminRole(user.getId());

        // then
        Optional<User> updated = userRepository.findById(user.getId());
        assertTrue(updated.isPresent());
        assertTrue(updated.get().getUserRoles().contains(UserRole.ADMIN));
        assertEquals("testuser", response.getUsername());
    }

    @Test
    void 관리자_권한_부여_실패_사용자없음() {
        // when & then
        AuthException exception = assertThrows(AuthException.class, () -> authService.grantAdminRole(99L));
        assertEquals("USER_NOT_FOUND", exception.getCode());
    }
}





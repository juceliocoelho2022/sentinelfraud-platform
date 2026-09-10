package br.com.jucelio.sentinelfraud.api;

import br.com.jucelio.sentinelfraud.service.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    @Test
    void shouldAuthenticateCredentialsAndReturnToken() {
        var authenticationManager = mock(AuthenticationManager.class);
        var tokenService = mock(JwtTokenService.class);
        var authenticated = UsernamePasswordAuthenticationToken.authenticated("admin", null, java.util.List.of());
        var expected = new TokenResponse("signed-token", "Bearer", Instant.parse("2026-09-10T13:00:00Z"));
        when(authenticationManager.authenticate(any())).thenReturn(authenticated);
        when(tokenService.issue(authenticated)).thenReturn(expected);

        var response = new AuthController(authenticationManager, tokenService)
                .token(new TokenRequest("admin", "admin-demo"));

        assertThat(response).isEqualTo(expected);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenService).issue(authenticated);
    }
}

package br.com.jucelio.sentinelfraud.service;

import br.com.jucelio.sentinelfraud.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenServiceTest {

    @Test
    void shouldIssueSignedTokenWithSubjectIssuerRoleAndExpiration() throws Exception {
        var config = new SecurityConfig();
        var rsaKey = config.rsaKey();
        var decoder = config.jwtDecoder(rsaKey, "https://sentinelfraud.local");
        var service = new JwtTokenService(config.jwtEncoder(rsaKey), "https://sentinelfraud.local", Duration.ofHours(1));
        var authentication = UsernamePasswordAuthenticationToken.authenticated("analyst", null,
                List.of(new SimpleGrantedAuthority("ROLE_ANALYST")));

        var response = service.issue(authentication);
        var jwt = decoder.decode(response.accessToken());

        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresAt()).isAfter(jwt.getIssuedAt());
        assertThat(jwt.getSubject()).isEqualTo("analyst");
        assertThat(jwt.getIssuer().toString()).isEqualTo("https://sentinelfraud.local");
        assertThat(jwt.getClaimAsStringList("roles")).containsExactly("ANALYST");
    }
}

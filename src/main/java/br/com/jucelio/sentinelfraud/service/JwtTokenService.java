package br.com.jucelio.sentinelfraud.service;

import br.com.jucelio.sentinelfraud.api.TokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class JwtTokenService {
    private final JwtEncoder encoder;
    private final String issuer;
    private final Duration ttl;

    public JwtTokenService(JwtEncoder encoder, @Value("${security.jwt.issuer}") String issuer,
            @Value("${security.jwt.ttl}") Duration ttl) {
        this.encoder = encoder;
        this.issuer = issuer;
        this.ttl = ttl;
    }

    public TokenResponse issue(Authentication authentication) {
        var now = Instant.now();
        var expiresAt = now.plus(ttl);
        var roles = authentication.getAuthorities().stream().map(authority -> authority.getAuthority())
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring(5)).toList();
        var claims = JwtClaimsSet.builder().issuer(issuer).issuedAt(now).expiresAt(expiresAt)
                .subject(authentication.getName()).claim("roles", roles).build();
        var token = encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        return new TokenResponse(token, "Bearer", expiresAt);
    }
}

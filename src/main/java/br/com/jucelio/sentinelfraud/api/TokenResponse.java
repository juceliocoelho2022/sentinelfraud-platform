package br.com.jucelio.sentinelfraud.api;

import java.time.Instant;

public record TokenResponse(String accessToken, String tokenType, Instant expiresAt) { }

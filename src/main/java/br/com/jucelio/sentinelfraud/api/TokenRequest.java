package br.com.jucelio.sentinelfraud.api;

import jakarta.validation.constraints.NotBlank;

public record TokenRequest(@NotBlank String username, @NotBlank String password) { }

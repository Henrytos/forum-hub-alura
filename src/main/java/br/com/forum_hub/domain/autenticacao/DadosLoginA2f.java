package br.com.forum_hub.domain.autenticacao;

import jakarta.validation.constraints.NotBlank;

public record DadosLoginA2f(
        @NotBlank String email,
        @NotBlank String codigo
) {
}

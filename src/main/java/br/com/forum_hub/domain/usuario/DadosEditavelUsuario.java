package br.com.forum_hub.domain.usuario;

import jakarta.validation.constraints.NotBlank;

public record DadosEditavelUsuario(
        @NotBlank String nomeCompleto,
        String miniBiografia,
        String biografia) {
}

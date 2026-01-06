package br.com.forum_hub.domain.usuario;

import br.com.forum_hub.domain.autenticacao.github.DadosUsuarioGitHub;
import jakarta.validation.constraints.NotBlank;

public record DadosCadastroUsuario(
        @NotBlank String email,
        @NotBlank String senha,
        @NotBlank String confirmacaoSenha,
        @NotBlank String nomeCompleto,
        @NotBlank String nomeUsuario,
        String miniBiografia,
        String biografia
) {

}

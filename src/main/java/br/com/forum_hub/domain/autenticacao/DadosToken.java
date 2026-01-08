package br.com.forum_hub.domain.autenticacao;

public record DadosToken(
        String token,
        String refreshToken,
        Boolean a2f
) {
}

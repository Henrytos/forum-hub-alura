package br.com.forum_hub.domain.resposta;

import br.com.forum_hub.domain.usuario.Usuario;

import java.time.LocalDateTime;

public record DadosListagemResposta(
        Long id,
        String mensagem,
        Usuario autor,
        LocalDateTime dataCriacao,
        Boolean solucao
) {
    public DadosListagemResposta(Resposta resposta) {
        this(resposta.getId(), resposta.getMensagem(), resposta.getAutor(), resposta.getDataCriacao(), resposta.ehSolucao());
    }
}

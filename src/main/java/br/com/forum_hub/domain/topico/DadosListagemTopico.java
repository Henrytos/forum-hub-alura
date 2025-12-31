package br.com.forum_hub.domain.topico;

import br.com.forum_hub.domain.resposta.DadosListagemResposta;
import br.com.forum_hub.domain.usuario.Usuario;

import java.time.LocalDateTime;
import java.util.List;

public record DadosListagemTopico(
        Long id,
        String titulo,
        String mensagem,
        Usuario autor,
        Status status,
        LocalDateTime dataCriacao,
        Integer quantidadeRespostas,
        String curso
) {
    public DadosListagemTopico(Topico topico) {
        this(topico.getId(), topico.getTitulo(), topico.getMensagem(), topico.getAutor(), topico.getStatus(), topico.getDataCriacao(), topico.getQuantidadeRespostas(), topico.getCurso().getNome());
    }
}

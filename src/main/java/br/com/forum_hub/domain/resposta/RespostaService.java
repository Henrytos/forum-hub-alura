package br.com.forum_hub.domain.resposta;

import br.com.forum_hub.domain.perfil.PerfilNome;
import br.com.forum_hub.domain.topico.Status;
import br.com.forum_hub.domain.topico.TopicoService;
import br.com.forum_hub.domain.usuario.Usuario;
import br.com.forum_hub.infra.exception.RegraDeNegocioException;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RespostaService {
    private final RespostaRepository repository;
    private final TopicoService topicoService;
    private final RoleHierarchy roleHierarchy;

    public RespostaService(RespostaRepository repository, TopicoService topicoService, RoleHierarchy roleHierarchy) {
        this.repository = repository;
        this.topicoService = topicoService;
        this.roleHierarchy = roleHierarchy;
    }

    @Transactional
    public Resposta cadastrar(DadosCadastroResposta dados, Usuario autor, Long idTopico) {
        var topico = topicoService.buscarPeloId(idTopico);

        if (!topico.estaAberto()) {
            throw new RegraDeNegocioException("O tópico está fechado! Você não pode adicionar mais respostas.");
        }

        if (topico.getQuantidadeRespostas() == 0) {
            topico.alterarStatus(Status.RESPONDIDO);
        }

        topico.incrementarRespostas();

        var resposta = new Resposta(dados, autor, topico);
        return repository.save(resposta);
    }

    @Transactional
    public Resposta atualizar(DadosAtualizacaoResposta dados, Usuario logado) {
        var resposta = buscarPeloId(dados.id());

        if (validoParaModificacao(resposta, logado))
            return resposta.atualizarInformacoes(dados);
        else
            throw new AccessDeniedException("Sem autorização para edição da resposta");
    }

    private boolean validoParaModificacao(Resposta resposta, Usuario logado) {

        var autoridades = logado.getAuthorities();
        for (GrantedAuthority autoridade : autoridades) {
            var autoridadesAlcancaveis = this.roleHierarchy.getReachableGrantedAuthorities(List.of(autoridade));

            for (GrantedAuthority perfil : autoridadesAlcancaveis) {
                if (perfil.getAuthority().equals(PerfilNome.MODERADOR.toString()))
                    return true;

                if (
                        perfil.getAuthority().equals(PerfilNome.ESTUDANTE.toString())
                                ||
                                perfil.getAuthority().equals(PerfilNome.INSTRUTOR.toString())
                                        &&
                                        resposta.getAutor().getId().equals(logado.getId())

                ) {
                    return true;
                }
            }

        }

        return false;
    }

    public List<Resposta> buscarRespostasTopico(Long id) {
        return repository.findByTopicoId(id);
    }

    @Transactional
    public Resposta marcarComoSolucao(Long id, Usuario logado) {
        var resposta = buscarPeloId(id);

        var topico = resposta.getTopico();
        if (topico.getStatus() == Status.RESOLVIDO)
            throw new RegraDeNegocioException("O tópico já foi solucionado! Você não pode marcar mais de uma resposta como solução.");

        if (validoParaModificacao(resposta, logado)) {
            topico.alterarStatus(Status.RESOLVIDO);
            return resposta.marcarComoSolucao();
        } else
            throw new AccessDeniedException("Sem autorização para edição da resposta");
    }

    @Transactional
    public void excluir(Long id, Usuario logado) {
        var resposta = buscarPeloId(id);
        var topico = resposta.getTopico();

        repository.deleteById(id);

        topico.decrementarRespostas();
        if (topico.getQuantidadeRespostas() == 0)
            topico.alterarStatus(Status.NAO_RESPONDIDO);
        else if (resposta.ehSolucao() && validoParaModificacao(resposta, logado))
            topico.alterarStatus(Status.RESPONDIDO);
    }

    public Resposta buscarPeloId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RegraDeNegocioException("Resposta não encontrada!"));
    }
}

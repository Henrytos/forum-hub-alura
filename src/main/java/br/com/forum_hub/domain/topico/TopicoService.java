package br.com.forum_hub.domain.topico;

import br.com.forum_hub.domain.curso.CursoService;
import br.com.forum_hub.domain.perfil.PerfilNome;
import br.com.forum_hub.domain.usuario.Usuario;
import br.com.forum_hub.infra.exception.RegraDeNegocioException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TopicoService {

    private final TopicoRepository repository;
    private final CursoService cursoService;
    private final RoleHierarchy roleHierarchy;

    public TopicoService(TopicoRepository repository, CursoService cursoService, RoleHierarchy roleHierarchy) {
        this.repository = repository;
        this.cursoService = cursoService;
        this.roleHierarchy = roleHierarchy;
    }

    @Transactional
    public Topico cadastrar(DadosCadastroTopico dados, Usuario autor) {
        var curso = cursoService.buscarPeloId(dados.cursoId());
        var topico = new Topico(dados,  autor, curso);
        return repository.save(topico);
    }
    public Page<DadosListagemTopico> listar(String categoria, Long idCurso, Boolean semResposta, Boolean solucionados, Pageable paginacao) {
        Specification<Topico> spec = Specification.where(TopicoSpecification.estaAberto())
                .and(TopicoSpecification.temCategoria(categoria))
                .and(TopicoSpecification.temCursoId(idCurso))
                .and(TopicoSpecification.estaSemResposta(semResposta))
                .and(TopicoSpecification.estaSolucionado(solucionados));

        Page<Topico> topicos = repository.findAll(spec, paginacao);
        return topicos.map(DadosListagemTopico::new);
    }

    @Transactional
    public Topico atualizar(DadosAtualizacaoTopico dados, Usuario logado) {
        var topico = buscarPeloId(dados.id());
        var curso = cursoService.buscarPeloId(dados.cursoId());

        if(validoParaModificacao(topico, logado))
            return topico.atualizarInformacoes(dados, curso);
        else
            throw new RegraDeNegocioException("Você não pode Atualizar o tópico .");
    }

    @Transactional
    public void excluir(Long id, Usuario logado) {
        var topico = buscarPeloId(id);

        if (topico.getStatus() == Status.NAO_RESPONDIDO && validoParaModificacao(topico, logado))
            repository.deleteById(id);
        else
            throw new RegraDeNegocioException("Você não pode apagar um tópico que já foi respondido.");
    }

    public Topico buscarPeloId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RegraDeNegocioException("Tópico não encontrado!"));

    }

    @Transactional
    public void fechar(Long id,  Usuario logado) {
        var topico = buscarPeloId(id);

        if(validoParaModificacao(topico, logado))
            topico.fechar();
        else
            throw new AccessDeniedException("Usuario não tem acesso a fechar ao topico");
    }

    private boolean validoParaModificacao(Topico topico, Usuario logado) {

        if(topico.getAutor().getId().equals(logado.getId()))
            return true;

        var autoridades = logado.getAuthorities();

        for (GrantedAuthority autoridade : autoridades) {
            var autoridadesAlcancaveis = this.roleHierarchy.getReachableGrantedAuthorities(List.of(autoridade));

            for (GrantedAuthority perfil : autoridadesAlcancaveis) {
                if(perfil.getAuthority().equals(PerfilNome.MODERADOR.toString()))
                    return true;
            }
        }

        return false;

    }

    private boolean validoParaModificacao(Topico topico, Usuario logado, PerfilNome perfilNome) {

        if(topico.getAutor().getId().equals(logado.getId()))
            return true;

        var autoridades = logado.getAuthorities();

        for (GrantedAuthority autoridade : autoridades) {
            var autoridadesAlcancaveis = this.roleHierarchy.getReachableGrantedAuthorities(List.of(autoridade));

            for (GrantedAuthority perfil : autoridadesAlcancaveis) {
                if(perfil.getAuthority().equals(perfilNome.toString()))
                    return true;
            }
        }

        return false;

    }
}

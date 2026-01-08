package br.com.forum_hub.domain.usuario;

import br.com.forum_hub.domain.autenticacao.github.DadosUsuarioGitHub;
import br.com.forum_hub.domain.perfil.Perfil;
import br.com.forum_hub.domain.perfil.PerfilNome;
import br.com.forum_hub.domain.perfil.PerfilRepostiroy;
import br.com.forum_hub.infra.email.EmailService;
import br.com.forum_hub.infra.exception.RegraDeNegocioException;
import br.com.forum_hub.infra.security.totp.TotpService;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RegistroService {

    private final PasswordEncoder passwordEncoder;

    private final UsuarioRepository usuarioRepository;

    private final EmailService emailService;

    private final PerfilRepostiroy perfilRepository;

    public RegistroService(PasswordEncoder passwordEncoder, UsuarioRepository usuarioRepository, EmailService emailService, PerfilRepostiroy perfilRepository, TotpService totpService) {
        this.passwordEncoder = passwordEncoder;
        this.usuarioRepository = usuarioRepository;
        this.emailService = emailService;
        this.perfilRepository = perfilRepository;
    }


    @Transactional
    public Usuario cadastrar(DadosCadastroUsuario dados) {
        Optional<Usuario> optionalUsuario = usuarioRepository
                .findByEmailIgnoreCaseOrNomeUsuarioIgnoreCaseAndVerificadoTrue(dados.email(), dados.nomeUsuario());

        if (optionalUsuario.isPresent()) {
            throw new RegraDeNegocioException("Já existe uma conta cadastrada com esse email ou nome de usuário!");
        }


        if (!dados.senha().equals(dados.confirmacaoSenha())) {
            throw new RegraDeNegocioException("Senha não bate com a confirmação!");
        }

        var senhaEncriptada = passwordEncoder.encode(dados.senha());

        Perfil perfilPadrao = this.perfilRepository.findByNome(PerfilNome.ESTUDANTE).orElseThrow();

        var usuario = new Usuario(dados, senhaEncriptada, perfilPadrao);

        emailService.enviarEmailVerificacao(usuario);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario cadastrar(DadosUsuarioGitHub dados, String email) {
        Optional<Usuario> optionalUsuario = usuarioRepository
                .findByEmailIgnoreCaseOrNomeUsuarioIgnoreCaseAndVerificadoTrue(email, dados.login());

        if (optionalUsuario.isPresent()) {
            throw new RegraDeNegocioException("Já existe uma conta cadastrada com esse email ou nome de usuário!");
        }

        Perfil perfilPadrao = this.perfilRepository.findByNome(PerfilNome.ESTUDANTE).orElseThrow();

        var usuario = new Usuario(dados, email, perfilPadrao);

        usuario = usuarioRepository.save(usuario);

        emailService.enviarEmailVerificacao(usuario);

        return usuario;
    }

    @Transactional
    public Usuario cadastrar(String email, String nomeCompleto){
        Optional<Usuario> optionalUsuario = usuarioRepository
                .findByEmailIgnoreCaseOrNomeUsuarioIgnoreCaseAndVerificadoTrue(email, nomeCompleto);

        if (optionalUsuario.isPresent()) {
            throw new RegraDeNegocioException("Já existe uma conta cadastrada com esse email ou nome de usuário!");
        }


        Perfil perfilPadrao = this.perfilRepository.findByNome(PerfilNome.ESTUDANTE).orElseThrow();

        Usuario usuario = new Usuario(email, nomeCompleto, perfilPadrao);

        return this.usuarioRepository.save(usuario);
    }


    @Transactional
    public void verificarEmail(String codigo) {
        Usuario usuario = usuarioRepository.findByToken(codigo).orElseThrow();
        usuario.verificar();
    }


}

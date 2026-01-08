package br.com.forum_hub.domain.usuario;

import br.com.forum_hub.domain.autenticacao.github.DadosUsuarioGitHub;
import br.com.forum_hub.domain.perfil.Perfil;
import br.com.forum_hub.domain.perfil.PerfilNome;
import br.com.forum_hub.domain.perfil.PerfilRepostiroy;
import br.com.forum_hub.infra.email.EmailService;
import br.com.forum_hub.infra.exception.RegraDeNegocioException;
import br.com.forum_hub.infra.security.totp.TotpService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.apache.logging.log4j.util.Strings;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioService {

    private final PasswordEncoder passwordEncoder;

    private final UsuarioRepository usuarioRepository;

    private final EmailService emailService;
    private final PerfilRepostiroy perfilRepository;
    private final TotpService totpService;

    public UsuarioService(PasswordEncoder passwordEncoder, UsuarioRepository usuarioRepository, EmailService emailService, PerfilRepostiroy perfilRepository, TotpService totpService) {
        this.passwordEncoder = passwordEncoder;
        this.usuarioRepository = usuarioRepository;
        this.emailService = emailService;
        this.perfilRepository = perfilRepository;
        this.totpService = totpService;
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

    public void desativarUsuario(Usuario usuario) {
        Usuario usuarioEncontrado = this.usuarioRepository.findByEmailIgnoreCaseAndVerificadoTrue(usuario.getEmail()).orElseThrow();

        if (usuario.getPerfies().contains(PerfilNome.ADMIN) || usuarioEncontrado.getId().equals(usuario.getId()))
            usuarioEncontrado.inativar();
        else
            throw new AccessDeniedException("Você não tem autorização para inativar esta conta");
    }

    public Usuario obterPerfil(Usuario usuario) {

        Usuario usuarioEncontrado = this.usuarioRepository.findByEmailIgnoreCaseAndVerificadoTrue(usuario.getEmail()).orElseThrow();

        return usuarioEncontrado;
    }

    public Usuario editarPerfil(DadosEditavelUsuario dados, String email) {

        Usuario usuarioEncontrado = this.usuarioRepository.findByEmailIgnoreCaseAndVerificadoTrue(email).orElseThrow();

        if (!Strings.isBlank(dados.biografia()))
            usuarioEncontrado.setBiografia(dados.biografia());

        if (!Strings.isBlank(dados.miniBiografia()))
            usuarioEncontrado.setMiniBiografia(dados.miniBiografia());

        if (!Strings.isBlank(dados.nomeCompleto()))
            usuarioEncontrado.setNomeCompleto(dados.nomeCompleto());

        return usuarioEncontrado;
    }

    public void solicitarMudancaDeSenha(@Valid String email) {

        Usuario usuarioEncontrado = this.usuarioRepository.findByEmailIgnoreCaseAndVerificadoTrue(email).orElseThrow();

        usuarioEncontrado.gerarToken();

        this.emailService.enviarEmailDeSenha(usuarioEncontrado);

    }

    public void alterarSenha(String codigo, DadosAlterarSenha dados) {
        Usuario usuario = this.usuarioRepository.findByToken(codigo).orElseThrow();
        usuario.validarExpiracaoToken();

        if (!dados.senha().equals(dados.confirmacaoSenha()))
            throw new RegraDeNegocioException("Senha não bate com a confirmação!");

        String senhaCriptografada = this.passwordEncoder.encode(dados.senha());
        usuario.setSenha(senhaCriptografada);
        usuario.invalidarToken();

    }

    @Transactional
    public Usuario adicionarPerfil(Long id, @Valid DadosPefil dados) {
        Usuario usuarioEncontrado = this.usuarioRepository.findById(id).orElseThrow();
        Perfil perfilEncontrado = this.perfilRepository.findByNome(dados.perfilNome()).orElseThrow();

        usuarioEncontrado.adicionarPerfil(perfilEncontrado);

        return usuarioEncontrado;
    }

    @Transactional
    public Usuario removerPerfil(Long id, @Valid DadosPefil dados) {
        Usuario usuarioEncontrado = this.usuarioRepository.findById(id).orElseThrow();
        Perfil perfilEncontrado = this.perfilRepository.findByNome(dados.perfilNome()).orElseThrow();

        usuarioEncontrado.removerPerfil(perfilEncontrado);

        return usuarioEncontrado;
    }

    @Transactional
    public void ativarUsuario(Long id, Usuario logado) {
        Usuario usuarioEncontrado = this.usuarioRepository.findById(id).orElseThrow();

        if (logado.getPerfies().contains(PerfilNome.ADMIN))
            usuarioEncontrado.ativar();
        else
            throw new AccessDeniedException("Você não tem autorização para inativar esta conta");
    }

    @Transactional
    public void desativarUsuario(Usuario usuario, Long id) {

        Usuario usuarioEncontrado = this.usuarioRepository.findById(id).orElseThrow();

        if (usuario.getPerfies().contains(PerfilNome.ADMIN) || usuarioEncontrado.getId().equals(usuario.getId()))
            usuarioEncontrado.inativar();
        else
            throw new AccessDeniedException("Você não tem autorização para inativar esta conta");
    }

    @Transactional
    public String gerarQrCode(Usuario logado) {
        String secret = this.totpService.gerarSecret();
        logado.gerarSecret(secret);
        this.usuarioRepository.save(logado);

        return this.totpService.gerarQrCode(logado);
    }
}

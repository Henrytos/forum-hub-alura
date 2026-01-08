package br.com.forum_hub.domain.usuario;

import br.com.forum_hub.domain.autenticacao.DadosLoginA2f;
import br.com.forum_hub.domain.autenticacao.DadosToken;
import br.com.forum_hub.domain.autenticacao.JwtService;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    private final JwtService jwtService;


    public UsuarioService(PasswordEncoder passwordEncoder, UsuarioRepository usuarioRepository, EmailService emailService, PerfilRepostiroy perfilRepository, TotpService totpService, JwtService jwtService) {
        this.passwordEncoder = passwordEncoder;
        this.usuarioRepository = usuarioRepository;
        this.emailService = emailService;
        this.perfilRepository = perfilRepository;
        this.totpService = totpService;
        this.jwtService = jwtService;
    }




    public void desativarUsuario(Usuario usuario) {
        Usuario usuarioEncontrado = this.usuarioRepository.findByEmailIgnoreCaseAndVerificadoTrue(usuario.getEmail()).orElseThrow();

        if (usuario.getPerfies().contains(PerfilNome.ADMIN) || usuarioEncontrado.getId().equals(usuario.getId()))
            usuarioEncontrado.inativar();
        else
            throw new AccessDeniedException("Você não tem autorização para inativar esta conta");
    }

    @Transactional
    public Usuario obterPerfil(Usuario usuario) {

        Usuario usuarioEncontrado = this.usuarioRepository.findByEmailIgnoreCaseAndVerificadoTrue(usuario.getEmail()).orElseThrow();

        return usuarioEncontrado;
    }

    @Transactional
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

    @Transactional
    public void solicitarMudancaDeSenha(@Valid String email) {

        Usuario usuarioEncontrado = this.usuarioRepository.findByEmailIgnoreCaseAndVerificadoTrue(email).orElseThrow();

        usuarioEncontrado.gerarToken();

        this.emailService.enviarEmailDeSenha(usuarioEncontrado);

    }

    @Transactional
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

    public void ativarA2f(String codigo, Usuario logado) {

        if(logado.a2fAtiva())
            throw new RegraDeNegocioException("Já está ativa");

        if(totpService.verificarCodigo(codigo, logado))
            throw new RegraDeNegocioException("Codigo invalido");

        logado.ativarA2f();
        usuarioRepository.save(logado);
    }

    public DadosToken efetuarLoginA2f(@Valid DadosLoginA2f dados) {
        Usuario usuario = this.usuarioRepository.findByEmailIgnoreCaseAndVerificadoTrue(dados.email()).orElseThrow();

        if(!usuario.a2fAtiva())
            throw new RegraDeNegocioException("Autenticação de 2 fatores não está ativo");

        Boolean codigoValido = this.totpService.verificarCodigo(dados.codigo(), usuario);

        if(!codigoValido)
            throw new RegraDeNegocioException("Codigo Inválido");

        Authentication authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // geração token jwt
        String token = this.jwtService.gerarToken(usuario);

        String refreshToken = this.jwtService.gerarRefreshToken(usuario);

        return new DadosToken(token, refreshToken, usuario.a2fAtiva());
    }
}

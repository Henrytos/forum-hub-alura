package br.com.forum_hub.domain.autenticacao.github;

import br.com.forum_hub.domain.autenticacao.DadosToken;
import br.com.forum_hub.domain.autenticacao.JwtService;
import br.com.forum_hub.domain.usuario.UsuarioService;
import br.com.forum_hub.domain.usuario.Usuario;
import br.com.forum_hub.domain.usuario.UsuarioRepository;
import br.com.forum_hub.infra.exception.RegraDeNegocioException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class LoginGitHubService {

    //    LOGIN
    @Value("${application.github.login.client.id}")
    private String CLIENT_ID;

    @Value("${application.github.login.redirect_uri}")
    private String REDIRECT_URI;

    @Value("${application.github.login.client.secret_key}")
    private String CLIENT_SECRET_KEY;

//    REGISTER

    @Value("${application.github.register.client.id}")
    private String REGISTER_CLIENT_ID;

    @Value("${application.github.register.redirect_uri}")
    private String REGISTER_REDIRECT_URI;

    @Value("${application.github.register.client.secret_key}")
    private String REGISTER_CLIENT_SECRET_KEY;


    private final RestClient restClient;

    private final UsuarioRepository usuarioRepository;

    private final JwtService jwtService;

    private final UsuarioService usuarioService;


    public LoginGitHubService(RestClient.Builder builder, UsuarioRepository usuarioRepository, JwtService jwtService, UsuarioService usuarioService) {
        this.restClient = builder.build();
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
        this.usuarioService = usuarioService;
    }


    public String obterUrl() {
        return "https://github.com/login/oauth/authorize?" +
                "client_id=" + CLIENT_ID +
                "&redirect_uri=" + REDIRECT_URI +
                "&scope=read:user,user:email,public_repo";
    }

    public String obterUrlRegistro() {
        return "https://github.com/login/oauth/authorize?" +
                "client_id=" + REGISTER_CLIENT_ID +
                "&redirect_uri=" + REGISTER_REDIRECT_URI +
                "&scope=read:user,user:email";
    }

    private String obterToken(String code, String clientId, String redirectUri, String clientSecret) {

        String resposta = restClient
                .post()
                .uri("https://github.com/login/oauth/access_token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "client_id", clientId,
                        "redirect_uri", redirectUri,
                        "client_secret", clientSecret,
                        "code", code))
                .retrieve()
                .body(Map.class)
                .get("access_token").toString();

        System.out.println(resposta);
        return resposta;
    }

    public String obterEmail(String code, String clientId, String redirectUri, String clientSecret) {
        String token = obterToken(code, clientId, redirectUri, clientSecret);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        DadosEmail[] resposta = restClient
                .get()
                .uri("https://api.github.com/user/emails")
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(DadosEmail[].class);


        for (DadosEmail dadosEmail : resposta) {
            if (dadosEmail.primary() && dadosEmail.verified())
                return dadosEmail.email();
        }

        throw new RegraDeNegocioException("Não tem conta aqui");
    }

    public String obterEmail(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        DadosEmail[] resposta = restClient
                .get()
                .uri("https://api.github.com/user/emails")
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(DadosEmail[].class);


        for (DadosEmail dadosEmail : resposta) {
            if (dadosEmail.primary() && dadosEmail.verified())
                return dadosEmail.email();
        }

        throw new RegraDeNegocioException("Não tem conta aqui");
    }


    @Transactional
    public Usuario registrar(String code) {
        String token = obterToken(code, REGISTER_CLIENT_ID, REGISTER_REDIRECT_URI, REGISTER_CLIENT_SECRET_KEY);

        String email = obterEmail(token);
        DadosUsuarioGitHub dados = this.obterUsuario(token);

        return usuarioService.cadastrar(dados,email);
    }

    private DadosUsuarioGitHub obterUsuario(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        DadosUsuarioGitHub usuario = this.restClient
                .get()
                .uri("https://api.github.com/user")
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(DadosUsuarioGitHub.class);

        return usuario;
    }

    public DadosToken logar(String code) {
        String email = this.obterEmail(code, CLIENT_ID, REDIRECT_URI, CLIENT_SECRET_KEY);

        Usuario usuario = usuarioRepository.findByEmailIgnoreCaseAndVerificadoTrue(email).orElseThrow(() ->
                new RegraDeNegocioException("Usuario não existe na aplicação"));
        Authentication authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = this.jwtService.gerarToken(usuario);
        String refreshToken = this.jwtService.gerarRefreshToken(usuario);

        return new DadosToken(token, refreshToken);
    }
}

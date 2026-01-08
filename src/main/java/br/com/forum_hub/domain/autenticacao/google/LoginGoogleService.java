package br.com.forum_hub.domain.autenticacao.google;

import br.com.forum_hub.domain.autenticacao.DadosToken;
import br.com.forum_hub.domain.autenticacao.JwtService;
import br.com.forum_hub.domain.usuario.Usuario;
import br.com.forum_hub.domain.usuario.UsuarioRepository;
import br.com.forum_hub.infra.exception.RegraDeNegocioException;
import com.auth0.jwt.JWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class LoginGoogleService {
    //    LOGIN
    @Value("${application.google.login.client.id}")
    private String CLIENT_ID;

    @Value("${application.google.login.redirect_uri}")
    private String REDIRECT_URI;

    @Value("${application.google.login.client.secret_key}")
    private String CLIENT_SECRET_KEY;

    private final UsuarioRepository usuarioRepository;

    private final JwtService jwtService;

    private final RestClient restClient;

    public LoginGoogleService(UsuarioRepository usuarioRepository, JwtService jwtService, RestClient.Builder builder) {
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
        restClient = builder.build();
    }


    public String obterUrl() {
        return "https://accounts.google.com/o/oauth2/v2/auth?" +
                "client_id=" + CLIENT_ID +
                "&redirect_uri=" + REDIRECT_URI +
                "&scope=https://www.googleapis.com/auth/userinfo.email" +
                "&response_type=code";
    }


    public DadosToken logar(String code) {
        String email = this.obterEmail(code);

        Usuario usuario = usuarioRepository.findByEmailIgnoreCaseAndVerificadoTrue(email).orElseThrow(() ->
                new RegraDeNegocioException("Usuario não existe na aplicação"));
        Authentication authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = this.jwtService.gerarToken(usuario);
        String refreshToken = this.jwtService.gerarRefreshToken(usuario);

        return new DadosToken(token, refreshToken, usuario.getA2fAtiva());
    }

    public String obterEmail(String code) {
        String token = obterToken(code);
        var decode = JWT.decode(token);
        System.out.println(decode.getClaims());
        return decode.getClaims().get("email").asString();
    }


    private String obterToken(String code) {

        String token = restClient
                .post()
                .uri("https://oauth2.googleapis.com/token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "client_id", CLIENT_ID,
                        "redirect_uri", REDIRECT_URI,
                        "client_secret", CLIENT_SECRET_KEY,
                        "code", code,
                        "grant_type", "authorization_code"))
                .retrieve()
                .body(Map.class)
                .get("id_token").toString();

        return token;
    }


}

package br.com.forum_hub.domain.autenticacao.google;

import br.com.forum_hub.domain.perfil.Perfil;
import br.com.forum_hub.domain.perfil.PerfilNome;
import br.com.forum_hub.domain.perfil.PerfilRepostiroy;
import br.com.forum_hub.domain.usuario.RegistroService;
import br.com.forum_hub.domain.usuario.Usuario;
import br.com.forum_hub.domain.usuario.UsuarioService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.util.Map;

@Service
public class CadastroGoogleService {

    private final RegistroService registroService;
    @Value("${application.google.login.client.id}")
    private String CLIENT_ID;

    @Value("${application.google.register.redirect_uri}")
    private String REDIRECT_URI;

    @Value("${application.google.login.client.secret_key}")
    private String CLIENT_SECRET;

    private final RestClient restClient;

    public CadastroGoogleService(RestClient.Builder builder, RegistroService registroService) {
        this.restClient = builder.build();
        this.registroService = registroService;
    }


    public String obterUrl() {
        return "https://accounts.google.com/o/oauth2/v2/auth?" +
                "client_id=" + CLIENT_ID +
                "&redirect_uri=" + REDIRECT_URI +
                "&scope=https://www.googleapis.com/auth/userinfo.email" +
                "%20https://www.googleapis.com/auth/userinfo.profile" +
                "&response_type=code";
    }

    public String obterToken(
            String code
    ) {

        return this.restClient
                .post()
                .uri("https://oauth2.googleapis.com/token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(
                        Map.of(
                                "client_id", CLIENT_ID,
                                "redirect_uri", REDIRECT_URI,
                                "client_secret", CLIENT_SECRET,
                                "code", code,
                                "grant_type", "authorization_code"
                        )
                )
                .retrieve()
                .body(Map.class)
                .get("id_token").toString();
    }


    public Usuario cadastrar(String code) {
        String token = this.obterToken(code);

        DecodedJWT decodedJWT = JWT.decode(token);
        String email = decodedJWT.getClaims().get("email").asString();
        String nomeCompleto = decodedJWT.getClaims().get("name").asString();

        System.out.println("email =" + email);
        System.out.println("nomeCompleto =" + nomeCompleto);

        return registroService.cadastrar(email, nomeCompleto);
    }
}

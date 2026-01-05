package br.com.forum_hub.domain.autenticacao.github;

import br.com.forum_hub.domain.autenticacao.DadosToken;
import br.com.forum_hub.domain.usuario.Usuario;
import br.com.forum_hub.domain.usuario.UsuarioRepository;
import br.com.forum_hub.infra.exception.RegraDeNegocioException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class LoginGitHubService {

    @Value("${application.client.id}")
    private String CLIENT_ID;

    @Value("${application.redirect_uri}")
    private String REDIRECT_URI;

    @Value("${application.client.secret_key}")
    private String CLIENT_SECRET_KEY;

    private final RestClient restClient;

    private final UsuarioRepository usuarioRepository;

    public LoginGitHubService(RestClient.Builder builder, UsuarioRepository usuarioRepository) {
        this.restClient = builder.build();
        this.usuarioRepository = usuarioRepository;
    }


    public String obterUrl() {
        return "https://github.com/login/oauth/authorize?" +
                "client_id=" + CLIENT_ID +
                "&redirect_uri=" + REDIRECT_URI +
                "&scope=read:user,user:email";
    }

    private String obterToken(String code) {
        String resposta = restClient
                .post()
                .uri("https://github.com/login/oauth/access_token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "client_id", CLIENT_ID,
                        "redirect_uri", REDIRECT_URI,
                        "client_secret", CLIENT_SECRET_KEY,
                        "code", code))
                .retrieve()
                .body(Map.class)
                .get("access_token").toString();

        return resposta;
    }

    public String obterEmail(String code){
        String token = this.obterToken(code);
        System.out.println(token);
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
            if(dadosEmail.primary() && dadosEmail.verified())
                return dadosEmail.email();
        }

        throw new RegraDeNegocioException("Não tem conta aqui");
    }

    public Usuario autenticarPorEmail(String email){
        Usuario usuario = usuarioRepository.findByEmailIgnoreCaseAndVerificadoTrue(email).orElseThrow();
        Authentication authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        return usuario;
        }
}

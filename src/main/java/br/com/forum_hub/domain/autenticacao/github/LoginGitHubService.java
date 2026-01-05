package br.com.forum_hub.domain.autenticacao.github;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
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

    public LoginGitHubService(RestClient.Builder builder) {
        this.restClient = builder.build();
    }


    public String obterUrl() {
        return "https://github.com/login/oauth/authorize?" +
                "client_id=" + CLIENT_ID +
                "&redirect_uri=" + REDIRECT_URI +
                "&scope:read:user,user:email";
    }

    public String obterToken(String code) {
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
                .body(String.class);

        return resposta;
    }
}

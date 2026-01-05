package br.com.forum_hub.controller;

import br.com.forum_hub.domain.autenticacao.DadosToken;
import br.com.forum_hub.domain.autenticacao.JwtService;
import br.com.forum_hub.domain.autenticacao.github.LoginGitHubService;
import br.com.forum_hub.domain.usuario.Usuario;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login/github")
public class LoginGithubController {

    private final LoginGitHubService loginGitHubService;

    private final JwtService jwtService;

    public LoginGithubController(LoginGitHubService loginGitHubService, JwtService jwtService) {
        this.loginGitHubService = loginGitHubService;
        this.jwtService = jwtService;
    }

    @GetMapping
    public ResponseEntity<Void> redirecionarProGitHu(){
        String url = loginGitHubService.obterUrl();
        System.out.println(url);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", url);

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @GetMapping("/autorizado")
    public ResponseEntity<DadosToken> autorizado(
            @RequestParam String code
    ){
        String email = this.loginGitHubService.obterEmail(code);

        Usuario usuario = this.loginGitHubService.autenticarPorEmail(email);

        String token = this.jwtService.gerarToken(usuario);
        String refreshToken = this.jwtService.gerarRefreshToken(usuario);

        return ResponseEntity.ok(new DadosToken(token, refreshToken));
    }
}

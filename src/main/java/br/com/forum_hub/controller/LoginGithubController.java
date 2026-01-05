package br.com.forum_hub.controller;

import br.com.forum_hub.domain.autenticacao.github.LoginGitHubService;
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

    public LoginGithubController(LoginGitHubService loginGitHubService) {
        this.loginGitHubService = loginGitHubService;
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
    public ResponseEntity<String> autorizado(
            @RequestParam String code
    ){
        String token = this.loginGitHubService.obterEmail(code);

        return ResponseEntity.ok(token);
    }
}

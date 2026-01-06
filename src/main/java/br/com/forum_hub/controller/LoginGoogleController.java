package br.com.forum_hub.controller;

import br.com.forum_hub.domain.autenticacao.DadosToken;
import br.com.forum_hub.domain.autenticacao.JwtService;
import br.com.forum_hub.domain.autenticacao.google.LoginGoogleService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login/google")
public class LoginGoogleController {
    private final LoginGoogleService loginGoogleService;

    public LoginGoogleController(LoginGoogleService loginGoogleService, JwtService jwtService) {
        this.loginGoogleService = loginGoogleService;
    }

    @GetMapping
    public ResponseEntity<Void> redirecionarProGitHu() {
        String url = loginGoogleService.obterUrl();
        System.out.println(url);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", url);

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @GetMapping("/autorizado")
    public ResponseEntity<DadosToken> autorizado(
            @RequestParam String code
    ) {
        DadosToken dadosToken = this.loginGoogleService.logar(code);

        return ResponseEntity.ok(dadosToken);
    }
}

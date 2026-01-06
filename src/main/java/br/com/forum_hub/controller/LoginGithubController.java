package br.com.forum_hub.controller;

import br.com.forum_hub.domain.autenticacao.DadosToken;
import br.com.forum_hub.domain.autenticacao.JwtService;
import br.com.forum_hub.domain.autenticacao.github.DadosUsuarioGitHub;
import br.com.forum_hub.domain.autenticacao.github.LoginGitHubService;
import br.com.forum_hub.domain.usuario.DadosListagemUsuario;
import br.com.forum_hub.domain.usuario.Usuario;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

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
    public ResponseEntity<Void> redirecionarProGitHu() {
        String url = loginGitHubService.obterUrl();
        System.out.println(url);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", url);

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @GetMapping("/autorizado")
    public ResponseEntity<DadosToken> autorizado(
            @RequestParam String code
    ) {
        DadosToken dadosToken = this.loginGitHubService.logar(code);

        return ResponseEntity.ok(dadosToken);
    }

    @GetMapping("/registro")
    public ResponseEntity<Void> redirecionarParaRegistro() {
        String url = this.loginGitHubService.obterUrlRegistro();

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(url));

        return ResponseEntity.status(HttpStatus.FOUND).headers(headers).build();
    }

    @GetMapping("/registro_autorizado")
    public ResponseEntity<DadosListagemUsuario> registrar(
            @RequestParam String code
    ){
        Usuario usuario = this.loginGitHubService.registrar(code);
        return ResponseEntity.ok(new DadosListagemUsuario(usuario));
    }

}

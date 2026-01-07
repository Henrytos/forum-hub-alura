package br.com.forum_hub.controller;

import br.com.forum_hub.domain.autenticacao.DadosToken;
import br.com.forum_hub.domain.autenticacao.JwtService;
import br.com.forum_hub.domain.autenticacao.google.CadastroGoogleService;
import br.com.forum_hub.domain.autenticacao.google.LoginGoogleService;
import br.com.forum_hub.domain.usuario.DadosListagemUsuario;
import br.com.forum_hub.domain.usuario.Usuario;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/login/google")
public class LoginGoogleController {
    private final LoginGoogleService loginGoogleService;

    private final CadastroGoogleService cadastroGoogleService;

    public LoginGoogleController(LoginGoogleService loginGoogleService, JwtService jwtService, CadastroGoogleService cadastroGoogleService) {
        this.loginGoogleService = loginGoogleService;
        this.cadastroGoogleService = cadastroGoogleService;
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

    @GetMapping("/registro")
    public ResponseEntity<Void> redirecionarParaRegistro() {
        String url = this.cadastroGoogleService.obterUrl();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(url));

        return ResponseEntity.status(HttpStatus.FOUND).headers(headers).build();
    }

    @GetMapping("/registro_autorizado")
    public ResponseEntity<DadosListagemUsuario> cadatrar(
            @RequestParam String code,
            UriComponentsBuilder uriComponentsBuilder
    ){
        Usuario usuario = this.cadastroGoogleService.cadastrar(code);

        URI uri = uriComponentsBuilder.buildAndExpand("/perfil").toUri();

        return ResponseEntity.created(uri).body(new DadosListagemUsuario(usuario));
    }
}

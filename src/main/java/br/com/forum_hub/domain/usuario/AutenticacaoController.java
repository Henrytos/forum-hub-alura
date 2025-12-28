package br.com.forum_hub.domain.usuario;

import br.com.forum_hub.domain.autenticacao.DadosLogin;
import br.com.forum_hub.domain.autenticacao.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/login")
@RestController
public class AutenticacaoController {

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    public AutenticacaoController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping
    public ResponseEntity<String> efetuarLogin(
            @Valid @RequestBody DadosLogin dadosLogin
    ) {
        // objeto de autenticação não validado
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(dadosLogin.email(), dadosLogin.senha());

        // objeto de autenticação validado
        Authentication authentication = this.authenticationManager.authenticate(authenticationToken);

        // geração token jwt
        String token = this.jwtService.gerar((Usuario) authentication.getPrincipal());

        return ResponseEntity.ok(token);
    }

}

package br.com.forum_hub.domain.usuario;

import br.com.forum_hub.domain.autenticacao.DadosAtualizarToken;
import br.com.forum_hub.domain.autenticacao.DadosLogin;
import br.com.forum_hub.domain.autenticacao.DadosToken;
import br.com.forum_hub.domain.autenticacao.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RequestMapping
@RestController
public class AutenticacaoController {

    private final AuthenticationManager authenticationManager;

    private final UsuarioRepository usuarioRepository;

    private final JwtService jwtService;

    public AutenticacaoController(AuthenticationManager authenticationManager, UsuarioRepository usuarioRepository, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<DadosToken> efetuarLogin(
            @Valid @RequestBody DadosLogin dadosLogin
    ) {
        // objeto de autenticação não validado
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(dadosLogin.email(), dadosLogin.senha());

        // objeto de autenticação validado
        Authentication authentication = this.authenticationManager.authenticate(authenticationToken);

        // geração token jwt
        String token = this.jwtService.gerarToken((Usuario) authentication.getPrincipal());

        String refreshToken = this.jwtService.gerarRefreshToken((Usuario) authentication.getPrincipal());

        return ResponseEntity.ok(new DadosToken(token, refreshToken));
    }


    @PostMapping("/atualizar-token")
    public ResponseEntity<DadosToken> atualizarToken(
            @Valid @RequestBody DadosAtualizarToken dadosAtualizarToken
    ) {
        Long id = Long.valueOf(this.jwtService.validarToken(dadosAtualizarToken.refreshToken()));

        if (id == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Usuario usuario = usuarioRepository.findById(id).orElseThrow();

        String token = this.jwtService.gerarToken(usuario);
        String refreshToken = this.jwtService.gerarRefreshToken(usuario);

        return ResponseEntity.ok(new DadosToken(token, refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> efetuarLogout(Authentication authentication) {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("Logout realizado com sucesso!");
    }

}

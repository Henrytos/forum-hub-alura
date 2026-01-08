package br.com.forum_hub.controller;

import br.com.forum_hub.domain.autenticacao.*;
import br.com.forum_hub.domain.usuario.Usuario;
import br.com.forum_hub.domain.usuario.UsuarioRepository;
import br.com.forum_hub.domain.usuario.UsuarioService;
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

    private final UsuarioService usuarioService;

    public AutenticacaoController(AuthenticationManager authenticationManager, UsuarioRepository usuarioRepository, JwtService jwtService, UsuarioService usuarioService) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    public ResponseEntity<DadosToken> efetuarLogin(
            @Valid @RequestBody DadosLogin dadosLogin
    ) {
        // objeto de autenticação não validado
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(dadosLogin.email(), dadosLogin.senha());

        // objeto de autenticação validado
        Authentication authentication = this.authenticationManager.authenticate(authenticationToken);

        Usuario usuario = (Usuario) authentication.getPrincipal();

        if(usuario.a2fAtiva())
            return ResponseEntity.ok(new DadosToken(null, null, true));

        // geração token jwt
        String token = this.jwtService.gerarToken(usuario);

        String refreshToken = this.jwtService.gerarRefreshToken(usuario);

        return ResponseEntity.ok(new DadosToken(token, refreshToken, false));
    }

    @PostMapping("/verificar-a2f")
    public ResponseEntity<DadosToken> verificarSegundoFator(
            @RequestBody @Valid DadosLoginA2f dados
    ){
        DadosToken dadosToken = usuarioService.efetuarLoginA2f(dados);

        return ResponseEntity.ok(dadosToken);
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

        return ResponseEntity.ok(new DadosToken(token, refreshToken, false));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> efetuarLogout(Authentication authentication) {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("Logout realizado com sucesso!");
    }

}

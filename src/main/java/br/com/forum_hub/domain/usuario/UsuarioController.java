package br.com.forum_hub.domain.usuario;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping
public class UsuarioController {

    private final UsuariService usuarioService;

    public UsuarioController(UsuariService usuarioService) {
        this.usuarioService = usuarioService;
    }


    @PostMapping("/registrar")
    public ResponseEntity<DadosListagemUsuario> cadastrar(@RequestBody @Valid DadosCadastroUsuario dados, UriComponentsBuilder uriBuilder) {
        var usuario = usuarioService.cadastrar(dados);
        var uri = uriBuilder.path("/{nomeUsuario}").buildAndExpand(usuario.getNomeUsuario()).toUri();
        return ResponseEntity.created(uri).body(new DadosListagemUsuario(usuario));

    }


    @GetMapping("/verificar-conta")
    public ResponseEntity<String> verficiarEmail(
            @RequestParam String codigo
    ) {

        usuarioService.verificarEmail(codigo);
        return ResponseEntity.ok("Conta verificada com sucesso!");

    }

    @Transactional
    @DeleteMapping("/desativar")
    public ResponseEntity desativarConta(
            @AuthenticationPrincipal Usuario usuario
    ) {
        this.usuarioService.desativarUsuario(usuario);

        return ResponseEntity.noContent().build();
    }

    @Transactional
    @GetMapping("/perfil")
    public ResponseEntity perfil(
            @AuthenticationPrincipal Usuario usuario
    ) {
        Usuario usuarioEncontrado = this.usuarioService.obterPerfil(usuario);

        return ResponseEntity.ok().body(new DadosListagemUsuario(usuarioEncontrado));
    }


    @PutMapping("/perfil")
    public ResponseEntity editarPerfil(
            @RequestBody @Valid DadosEditavelUsuario dados,
            @AuthenticationPrincipal Usuario usuario
    ){
        Usuario usuarioEditado = this.usuarioService.editarPerfil(dados, usuario.getEmail());

        return ResponseEntity.ok().body(new DadosListagemUsuario(usuarioEditado));

    }

}

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

    private final UsuarioService usuarioService;


    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Transactional
    @PatchMapping("/alterar-senha")
    public ResponseEntity<String> alterSenha(
            @RequestParam String codigo,
            @RequestBody @Valid DadosAlterarSenha dados
    ) {
        this.usuarioService.alterarSenha(codigo, dados);

        return ResponseEntity.ok("Senha alterada com sucesso");
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
    @DeleteMapping("/desativar/{id}")
    public ResponseEntity desativarContaDeUmUsuario(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario
    ) {
        this.usuarioService.desativarUsuario(usuario, id);

        return ResponseEntity.noContent().build();
    }


    @Transactional
    @DeleteMapping("/ativar/{id}")
    public ResponseEntity ativarConta(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario logado
    ) {
        this.usuarioService.ativarUsuario(id, logado);

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
    ) {
        Usuario usuarioEditado = this.usuarioService.editarPerfil(dados, usuario.getEmail());

        return ResponseEntity.ok().body(new DadosListagemUsuario(usuarioEditado));

    }


    @Transactional
    @PostMapping("/solicitar-senha")
    public ResponseEntity solicitarMudancaDeSenha(
            @RequestBody @Valid DadosSolicitarSenha dados
    ) {
        this.usuarioService.solicitarMudancaDeSenha(dados.email());

        return ResponseEntity.noContent().build();
    }


}

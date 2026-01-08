package br.com.forum_hub.controller;

import br.com.forum_hub.domain.usuario.*;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
public class PerfilController {
    private final UsuarioService usuarioService;

    public PerfilController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }



    @PatchMapping("/adicionar-perfil/{id}")
    public ResponseEntity<DadosListagemUsuario> adicionarPerfil(
            @PathVariable Long id,
            @RequestBody @Valid DadosPefil dados
    ){

        Usuario usuarioAtualizado = usuarioService.adicionarPerfil(id, dados);

        return ResponseEntity.ok(new DadosListagemUsuario(usuarioAtualizado));
    }

    @PatchMapping("/remover-perfil/{id}")
    public ResponseEntity<DadosListagemUsuario> removerPerfil(
            @PathVariable Long id,
            @RequestBody @Valid DadosPefil dados
    ){

        Usuario usuarioAtualizado = usuarioService.removerPerfil(id, dados);

        return ResponseEntity.ok(new DadosListagemUsuario(usuarioAtualizado));
    }
}

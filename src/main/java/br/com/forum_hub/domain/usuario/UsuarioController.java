package br.com.forum_hub.domain.usuario;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping
public class UsuarioController {

    private final UsuariService usuarioService;

    public UsuarioController(UsuariService usuarioService) {
        this.usuarioService = usuarioService;
    }


    @PostMapping("/registrar")
    public ResponseEntity<DadosListagemUsuario> cadastrar(@RequestBody @Valid DadosCadastroUsuario dados, UriComponentsBuilder uriBuilder){
        var usuario = usuarioService.cadastrar(dados);
        var uri = uriBuilder.path("/{nomeUsuario}").buildAndExpand(usuario.getNomeUsuario()).toUri();
        return ResponseEntity.created(uri).body(new DadosListagemUsuario(usuario));

    }

}

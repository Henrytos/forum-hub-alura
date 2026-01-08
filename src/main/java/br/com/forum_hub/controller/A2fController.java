package br.com.forum_hub.controller;

import br.com.forum_hub.domain.usuario.Usuario;
import br.com.forum_hub.domain.usuario.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping
@RestController
public class A2fController {
    private final UsuarioService usuarioService;



    public A2fController(UsuarioService usuarioService) {

        this.usuarioService = usuarioService;

    }

    @PatchMapping("/configurar-a2f")
    public ResponseEntity<String> configurarA2f(
            @AuthenticationPrincipal Usuario logado
    ) {
        String qrCode = this.usuarioService.gerarQrCode(logado);

        return ResponseEntity.ok(qrCode);
    }



//    @PatchMapping("ativar-a2f")
//
//    public ResponseEntity<Void> ativarA2f(@RequestParam String codigo, @AuthenticationPrincipal Usuario logado){
//
//        usuarioService.ativarA2f(codigo, logado);
//
//        return ResponseEntity.noContent().build();
//
//    }

}

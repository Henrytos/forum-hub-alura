package br.com.forum_hub.domain.usuario;

import br.com.forum_hub.domain.perfil.PerfilNome;
import jakarta.validation.constraints.NotNull;

public record DadosPefil(
        @NotNull PerfilNome perfilNome
        ) {
}

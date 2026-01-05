package br.com.forum_hub.domain.perfil;

import br.com.forum_hub.domain.usuario.Usuario;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Entity
@Table(name = "perfies")
public class Perfil implements GrantedAuthority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true)
    private PerfilNome nome;

    @Override
    public String getAuthority() {
        return "ROLE_" + this.nome.toString();
    }

    @ManyToMany(mappedBy = "perfies")
    private Collection<Usuario> usuarios_perfies;

    public Collection<Usuario> getUsuarios_perfies() {
        return usuarios_perfies;
    }

    public void setUsuarios_perfies(Collection<Usuario> usuarios_perfies) {
        this.usuarios_perfies = usuarios_perfies;
    }
}

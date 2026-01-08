package br.com.forum_hub.domain.usuario;

import br.com.forum_hub.domain.autenticacao.github.DadosUsuarioGitHub;
import br.com.forum_hub.domain.perfil.Perfil;
import br.com.forum_hub.infra.exception.RegraDeNegocioException;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "usuarios")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String senha;
    private String nomeCompleto;
    private String nomeUsuario;
    private String miniBiografia;
    private String biografia;

    private String token;
    private LocalDateTime expiracaoToken;
    private Boolean verificado;

    private String secret;
    private Boolean a2fAtiva = false;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "usuarios_perfies",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "perfil_id")
    )
    private List<Perfil> perfies = new ArrayList<>();


    public Usuario(DadosCadastroUsuario dados, String senhaCriptografada, Perfil perfil) {
        this.nomeCompleto = dados.nomeCompleto();
        this.email = dados.email();
        this.senha = senhaCriptografada;
        this.nomeUsuario = dados.nomeUsuario();
        this.biografia = dados.biografia();
        this.miniBiografia = dados.miniBiografia();

        this.gerarToken();
        this.verificado = false;
        this.perfies.add(perfil);
    }

    public Usuario(DadosUsuarioGitHub dados, @Email String emailPrincipal, Perfil perfil) {
        this.nomeCompleto = dados.name();
        this.email = emailPrincipal;
        this.senha = "";
        this.nomeUsuario = dados.login();
        this.biografia = dados.bio();
        this.miniBiografia = "";

        this.verificado = true;
        this.perfies.add(perfil);
    }

    public Usuario(@Email String email, @NotBlank String nomeCompleto, Perfil perfil) {
        this.nomeCompleto = nomeCompleto;
        this.email = email;
        this.senha = "";
        this.nomeUsuario = email;
        this.biografia = "";
        this.miniBiografia = "";

        this.verificado = true;
        this.perfies.add(perfil);
    }


    public void gerarToken() {
        this.token = UUID.randomUUID().toString();
        this.expiracaoToken = LocalDateTime.now().plusMinutes(30);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return perfies;
    }

    @Override
    public String getPassword() {
        return this.senha;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

    public void verificar() {
        this.verificado = true;
        this.token = null;
        this.expiracaoToken = null;
    }

    public void inativar() {
        this.verificado = false;
    }

    public void validarExpiracaoToken() {
        if (this.getExpiracaoToken().isBefore(LocalDateTime.now()))
            throw new RegraDeNegocioException("Token expirou");
    }

    public void invalidarToken() {
        this.token = null;
        this.expiracaoToken = null;
    }

    public void adicionarPerfil(Perfil perfil) {
        this.perfies.add(perfil);
    }

    public void removerPerfil(Perfil perfil) {
        if (!this.perfies.contains(perfil))
            throw new RegraDeNegocioException("Perfil não existe neste usuario");

        this.perfies.remove(perfil);
    }

    public void ativar() {
        this.verificado = true;
    }

    public void gerarSecret(String secret) {
        this.secret = secret;
    }
}

package br.com.forum_hub.domain.usuario;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    Optional<Usuario> findByEmailIgnoreCaseOrNomeUsuarioIgnoreCase(String email, String nomeUsuario);
}

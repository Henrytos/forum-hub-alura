package br.com.forum_hub.domain.perfil;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PerfilRepostiroy extends JpaRepository<Perfil, Long> {
    Optional<Perfil> findByNome(PerfilNome perfilNome);
}

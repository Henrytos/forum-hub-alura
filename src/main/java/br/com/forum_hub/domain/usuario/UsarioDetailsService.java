package br.com.forum_hub.domain.usuario;

import br.com.forum_hub.infra.exception.RegraDeNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.usuarioRepository.findByEmailIgnoreCaseAndVerificadoTrue(username)
                .orElseThrow(() -> new RegraDeNegocioException("Usuário não existe na aplicação"));
    }

}

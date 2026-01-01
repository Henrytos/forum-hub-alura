package br.com.forum_hub.domain.usuario;

import br.com.forum_hub.infra.email.EmailService;
import br.com.forum_hub.infra.exception.RegraDeNegocioException;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuariService {

    private final PasswordEncoder passwordEncoder;

    private final UsuarioRepository usuarioRepository;

    private final EmailService emailService;

    public UsuariService(PasswordEncoder passwordEncoder, UsuarioRepository usuarioRepository, EmailService emailService) {
        this.passwordEncoder = passwordEncoder;
        this.usuarioRepository = usuarioRepository;
        this.emailService = emailService;
    }


    @Transactional
    public Usuario cadastrar(DadosCadastroUsuario dados) {
        Optional<Usuario> optionalUsuario = usuarioRepository
                .findByEmailIgnoreCaseOrNomeUsuarioIgnoreCaseAndVerificadoTrue(dados.email(), dados.nomeUsuario());

        if(optionalUsuario.isPresent()){
            throw new RegraDeNegocioException("Já existe uma conta cadastrada com esse email ou nome de usuário!");
        }


        if (!dados.senha().equals(dados.confirmacaoSenha())) {
            throw new RegraDeNegocioException("Senha não bate com a confirmação!");
        }

        var senhaEncriptada = passwordEncoder.encode(dados.senha());

        var usuario = new Usuario(dados, senhaEncriptada);
        emailService.enviarEmailVerificacao(usuario);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void verificarEmail(String codigo) {
            Usuario usuario = usuarioRepository.findByToken(codigo).orElseThrow();
            usuario.verificar();
    }
}

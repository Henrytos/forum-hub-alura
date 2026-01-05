package br.com.forum_hub.domain.autenticacao;

import br.com.forum_hub.domain.usuario.Usuario;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

@Service
public class JwtService {

    @Value("${application.jwt.secret_key}")
    private String JWT_SECRET_KEY;

    public String gerarToken(Usuario usuario) {
        Algorithm algorithm = Algorithm.HMAC256(JWT_SECRET_KEY);

        return JWT.create()
                .withIssuer("Forum Hub")
                .withSubject(usuario.getEmail())
                .withExpiresAt(expiracao(30))
                .sign(algorithm);
    }

    public String gerarRefreshToken(Usuario usuario) {
        Algorithm algorithm = Algorithm.HMAC256(JWT_SECRET_KEY);

        return JWT.create()
                .withIssuer("Forum Hub")
                .withSubject(usuario.getId().toString())
                .withExpiresAt(expiracao(120))
                .sign(algorithm);
    }

    public String validarToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(JWT_SECRET_KEY);

        try {
            return JWT
                    .require(algorithm)
                    .withIssuer("Forum Hub")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    private Instant expiracao(int tempoEmMinutos) {
        return Instant.now().plus(Duration.ofMinutes(tempoEmMinutos)).atOffset(ZoneOffset.ofHours(3)).toInstant();
    }

}

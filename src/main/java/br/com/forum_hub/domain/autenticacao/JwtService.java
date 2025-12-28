package br.com.forum_hub.domain.autenticacao;

import br.com.forum_hub.domain.usuario.Usuario;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

@Service
public class JwtService {


    public String gerar(Usuario usuario){
        Algorithm algorithm = Algorithm.HMAC256("1234");

        return JWT.create()
                .withIssuer("Forum Hub")
                .withSubject(usuario.getEmail())
                .withExpiresAt(Instant.now().plus(Duration.ofMinutes(10)).atOffset(ZoneOffset.ofHours(3)).toInstant())
                .sign(algorithm);
    }

}

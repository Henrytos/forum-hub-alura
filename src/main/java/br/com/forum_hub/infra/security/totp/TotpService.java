package br.com.forum_hub.infra.security.totp;

import br.com.forum_hub.domain.usuario.Usuario;
import com.atlassian.onetime.service.RandomSecretProvider;
import org.springframework.stereotype.Service;

@Service
public class TotpService {

    public String gerarSecret() {
        return new RandomSecretProvider().generateSecret().getBase32Encoded();
    }

    public String gerarQrCode(Usuario logado) {
        // Otpauth URL: otpauth://totp/<Issuer>:<User>?secret=<Secret>&issuer=<Issuer>
        String issuer = "Fórum Hub";

        return """
                otpauth://totp/%s:%s?secret=%s&issuer=%s
                """.formatted(issuer, logado.getNomeUsuario(), logado.getSecret(), issuer);
    }
}

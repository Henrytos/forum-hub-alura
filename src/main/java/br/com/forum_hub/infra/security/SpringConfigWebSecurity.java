package br.com.forum_hub.infra.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.lang.reflect.Array;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SpringConfigWebSecurity {

    private final TokenJwtFiltro tokenJwtFiltro;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity)
            throws Exception {

        return httpSecurity
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        req -> {
                            req.requestMatchers(HttpMethod.GET, "/cursos").permitAll();
                            req.requestMatchers(HttpMethod.GET, "/topicos/**").permitAll();

                            req.requestMatchers(HttpMethod.POST, "/topicos").hasRole("ESTUDANTE");
                            req.requestMatchers(HttpMethod.PUT, "/topicos").hasRole("ESTUDANTE");
                            req.requestMatchers(HttpMethod.DELETE, "/topicos/**").hasRole("ESTUDANTE");

                            req.requestMatchers(HttpMethod.PATCH, "/topicos/**").hasRole("MODERADOR");

                            req.requestMatchers(HttpMethod.PATCH, "/adicionar-perfil/**").hasRole("ADMIN");


                            req.requestMatchers(
                                                    "/atualizar-token",
                                                    "/login/**",
                                                    "/registrar",
                                                    "/verificar-conta",
                                                    "/alterar-senha",
                                                    "/solicitar-senha"
                                    ).permitAll()
                                    .anyRequest().authenticated();
                        }
                )
                .addFilterBefore(tokenJwtFiltro, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder encriptador() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    //    @Bean
//    public RoleHierarchy hierarquia(){
//        String hierarquia = """
//                ROLE_ADMIN > ROLE_MODERADOR
//                ROLE_MODERADOR > ROLE_INSTRUTOR
//                ROLE_MODERADOR > ROLE_ESTUDANTE
//                """;
//
//        return RoleHierarchyImpl.fromHierarchy(hierarquia);
//    }

    @Bean
    public RoleHierarchy hierarquia() {
        return RoleHierarchyImpl
                .withRolePrefix("ROLE")
                .role("ADMIN").implies("MODERADOR")
                .role("MODERADOR").implies("INSTRUTOR", "ESTUDANTE")
                .build();
    }
//
//    @Bean
//    public RoleHierarchy hierarquia() {
//        return RoleHierarchyImpl
//                .withDefaultRolePrefix()
//                .role("ADMIN").implies("MODERADOR")
//                .role("MODERADOR").implies("INSTRUTOR", "ESTUDANTE")
//                .build();
//    }

}

package com.colegiosapiens.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Configuração de segurança: início de sessão, permissões por perfil e respostas JSON.
 *
 * A autenticação é feita por sessão (cookie). O JavaScript envia o formulário para
 * /api/auth/login e o navegador guarda o cookie automaticamente.
 */
@Configuration
@EnableWebSecurity
public class SegurancaConfig {

    /** Origens do frontend separado autorizadas (application.properties: sapiens.cors.origens). */
    @Value("${sapiens.cors.origens:http://localhost:5500,http://localhost:3000,http://localhost:5173,http://localhost:8081}")
    private String origensPermitidas;

    /**
     * CORS: permite que o frontend servido noutra porta (ex.: Live Server em :5500)
     * chame a API e envie o cookie de sessão.
     */
    @Bean
    public CorsConfigurationSource configuracaoCors() {
        CorsConfiguration configuracao = new CorsConfiguration();
        configuracao.setAllowedOrigins(Arrays.stream(origensPermitidas.split(","))
                .map(String::trim).filter(o -> !o.isEmpty()).toList());
        configuracao.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuracao.setAllowedHeaders(List.of("*"));
        configuracao.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource fonte = new UrlBasedCorsConfigurationSource();
        fonte.registerCorsConfiguration("/**", configuracao);
        return fonte;
    }

    /** Codificador de senhas: as senhas ficam guardadas com BCrypt, nunca em texto simples. */
    @Bean
    public PasswordEncoder codificadorDeSenhas() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filtroDeSeguranca(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(configuracaoCors()))

            // CSRF desactivado para simplificar o projecto académico.
            // Em produção deve ser activado e o token enviado pelo JavaScript.
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth
                // Login e logout são públicos; consultar o utilizador actual exige sessão
                .requestMatchers("/api/auth/login", "/api/auth/logout", "/api/auth/registo").permitAll()
                .requestMatchers("/api/auth/eu").authenticated()

                // US33: só o administrador gere contas de acesso
                .requestMatchers("/api/utilizadores/**").hasRole("ADMINISTRADOR")

                // US01: só o administrador cadastra professores
                .requestMatchers(HttpMethod.POST, "/api/professores/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.GET, "/api/professores/**").hasAnyRole("ADMINISTRADOR", "SECRETARIA")

                // US28: só o administrador cadastra turmas; secretaria pode consultar
                .requestMatchers(HttpMethod.POST, "/api/turmas/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.GET, "/api/turmas/**").hasAnyRole("ADMINISTRADOR", "SECRETARIA")

                // US26, US27 e US11: secretaria e administrador
                .requestMatchers("/api/encarregados/**", "/api/alunos/**", "/api/vinculos/**")
                        .hasAnyRole("ADMINISTRADOR", "SECRETARIA")

                // Qualquer outra rota da API exige sessão iniciada
                .requestMatchers("/api/**").authenticated()

                // Páginas HTML, CSS e JS são públicos (os dados estão protegidos na API)
                .anyRequest().permitAll())

            // US32: início de sessão com identificador (e-mail ou telefone) e senha
            .formLogin(form -> form
                .loginProcessingUrl("/api/auth/login")
                .usernameParameter("identificador")
                .passwordParameter("senha")
                .successHandler((req, res, auth) ->
                        escreverJson(res, HttpServletResponse.SC_OK, "Sessão iniciada com sucesso."))
                .failureHandler((req, res, ex) ->
                        escreverJson(res, HttpServletResponse.SC_UNAUTHORIZED, "Credenciais inválidas ou conta desactivada."))
                .permitAll())

            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler((req, res, auth) ->
                        escreverJson(res, HttpServletResponse.SC_OK, "Sessão terminada.")))

            // Respostas JSON para acesso sem sessão (401) e sem permissão (403)
            .exceptionHandling(erros -> erros
                .authenticationEntryPoint((req, res, ex) ->
                        escreverJson(res, HttpServletResponse.SC_UNAUTHORIZED, "É necessário iniciar sessão."))
                .accessDeniedHandler((req, res, ex) ->
                        escreverJson(res, HttpServletResponse.SC_FORBIDDEN, "Não tem permissão para esta operação.")));

        return http.build();
    }

    /** Escreve uma resposta JSON simples com o estado HTTP indicado. */
    private static void escreverJson(HttpServletResponse resposta, int estado, String mensagem) throws IOException {
        resposta.setStatus(estado);
        resposta.setCharacterEncoding("UTF-8");
        resposta.setContentType("application/json;charset=UTF-8");
        resposta.getWriter().write("{\"estado\":" + estado + ",\"mensagem\":\"" + mensagem + "\"}");
    }
}

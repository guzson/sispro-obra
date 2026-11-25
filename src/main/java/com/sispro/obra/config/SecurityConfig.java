package com.sispro.obra.config;

import com.sispro.obra.dao.UsuarioDAO;
import com.sispro.obra.service.LoginAttemptService;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
public class SecurityConfig {
    private static final Logger LOG = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    UserDetailsService userDetailsService(UsuarioDAO usuarios) {
        return correo -> {
            var u = usuarios.findByCorreoIgnoreCase(correo)
                    .orElseThrow(() -> new UsernameNotFoundException("Credenciales invalidas"));
            boolean bloqueada = u.bloqueadoHasta != null && u.bloqueadoHasta.isAfter(LocalDateTime.now());
            return User.withUsername(u.correo)
                    .password(u.passwordHash)
                    .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + u.rol.name())))
                    .accountLocked(bloqueada)
                    .disabled(!u.activo)
                    .build();
        };
    }

    @Bean
    SecurityFilterChain security(HttpSecurity http, LoginAttemptService intentos) throws Exception {
        return http.authorizeHttpRequests(a -> a
                .requestMatchers("/login", "/registro", "/css/**", "/js/**", "/error").permitAll()
                .requestMatchers("/usuarios/**", "/auditoria/**").hasRole("ADMINISTRADOR")
                .requestMatchers("/obras/nueva", "/obras/guardar", "/obras/*/editar", "/obras/*/cerrar")
                    .hasRole("ADMINISTRADOR")
                .requestMatchers("/obras/*/etapas/nueva", "/obras/*/tareas/nueva", "/obras/*/asignar",
                                 "/incidencias/*/asignar", "/incidencias/*/anular")
                    .hasAnyRole("ADMINISTRADOR", "SUPERVISOR")
                .anyRequest().authenticated())
            .formLogin(f -> f.loginPage("/login")
                // Un solo mensaje para usuario inexistente, clave incorrecta o cuenta bloqueada.
                .failureHandler((req, res, ex) -> {
                    try { intentos.registrarFallo(req.getParameter("username")); }
                    catch (RuntimeException e) { LOG.error("No se pudo registrar el intento fallido", e); }
                    res.sendRedirect(req.getContextPath() + "/login?error");
                })
                .successHandler((req, res, auth) -> {
                    try { intentos.registrarExito(auth.getName()); }
                    catch (RuntimeException e) { LOG.error("No se pudo registrar el inicio de sesión", e); }
                    res.sendRedirect(req.getContextPath() + "/");
                })
                .permitAll())
            .logout(l -> l.logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID"))
            .headers(h -> h
                .contentSecurityPolicy(c -> c.policyDirectives(
                    "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; "
                    + "img-src 'self' data:; form-action 'self'; frame-ancestors 'none'; base-uri 'self'"))
                .referrerPolicy(r -> r.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER)))
            .build();
    }
}

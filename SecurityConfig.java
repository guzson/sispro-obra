package com.sispro.obra.config;

import com.sispro.obra.dao.UsuarioDAO;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    UserDetailsService userDetailsService(UsuarioDAO usuarios) {
        return correo -> {
            var u = usuarios.findByCorreoIgnoreCase(correo)
                    .orElseThrow(() -> new UsernameNotFoundException("Credenciales invalidas"));
            return new User(u.correo, u.passwordHash,
                    List.of(new SimpleGrantedAuthority("ROLE_" + u.rol.name())));
        };
    }

    @Bean
    SecurityFilterChain security(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(a -> a
                .requestMatchers("/login", "/registro", "/css/**").permitAll()
                .requestMatchers("/usuarios/**", "/auditoria/**").hasRole("ADMINISTRADOR")
                .requestMatchers("/obras/nueva", "/obras/guardar", "/obras/*/editar", "/obras/*/cerrar")
                    .hasRole("ADMINISTRADOR")
                .requestMatchers("/obras/*/etapas/nueva", "/obras/*/tareas/nueva", "/obras/*/asignar",
                                 "/incidencias/*/estado", "/incidencias/*/asignar", "/incidencias/*/anular")
                    .hasAnyRole("ADMINISTRADOR", "SUPERVISOR")
                .anyRequest().authenticated())
            .formLogin(f -> f.loginPage("/login").defaultSuccessUrl("/", true).permitAll())
            .logout(l -> l.logoutSuccessUrl("/login?logout"))
            .build();
    }
}

package com.sispro.obra.service;
import com.sispro.obra.dao.UsuarioDAO;
import com.sispro.obra.model.Usuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
@Service public class UsuarioService {
 private final UsuarioDAO dao; private final PasswordEncoder encoder;
 public UsuarioService(UsuarioDAO dao,PasswordEncoder encoder){this.dao=dao;this.encoder=encoder;}
 public Usuario registrar(Usuario usuario,String clave){if(dao.existsByCorreoIgnoreCase(usuario.correo))throw new IllegalArgumentException("El correo ya está registrado.");if(clave==null||!clave.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$"))throw new IllegalArgumentException("Contraseña: mínimo 8 caracteres, mayúscula, minúscula y número.");usuario.passwordHash=encoder.encode(clave);return dao.save(usuario);}
}

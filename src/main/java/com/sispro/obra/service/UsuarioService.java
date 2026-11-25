package com.sispro.obra.service;
import com.sispro.obra.dao.UsuarioDAO;
import com.sispro.obra.model.Rol;
import com.sispro.obra.model.Usuario;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
@Service public class UsuarioService {
 private static final String REGEX_CLAVE="^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,64}$";
 private final UsuarioDAO dao; private final PasswordEncoder encoder;
 public UsuarioService(UsuarioDAO dao,PasswordEncoder encoder){this.dao=dao;this.encoder=encoder;}

 public void validarClave(String clave){
  if(clave==null||!clave.matches(REGEX_CLAVE))throw new IllegalArgumentException("Contraseña: de 8 a 64 caracteres, con mayúscula, minúscula y número.");
 }

 /** Alta hecha por un administrador (ya autenticado): puede avisar que el correo existe. */
 public Usuario registrar(Usuario usuario,String clave){
  validarClave(clave);
  if(dao.existsByCorreoIgnoreCase(usuario.correo))throw new IllegalArgumentException("El correo ya está registrado.");
  return guardar(usuario,clave);
 }

 /** Registro público: nunca revela si el correo ya existía. Devuelve true solo si creó la cuenta. */
 public boolean registrarPublico(Usuario usuario,String clave){
  validarClave(clave);
  if(dao.existsByCorreoIgnoreCase(usuario.correo))return false;
  try{guardar(usuario,clave);return true;}
  catch(DataIntegrityViolationException e){return false;}
 }

 /** RF51: edita nombre, teléfono y rol de otro usuario. */
 public void actualizar(Long id,String nombre,String telefono,Rol rol,Usuario actual){
  Usuario u=dao.findById(id).orElseThrow(()->new IllegalArgumentException("Usuario no encontrado."));
  if(nombre==null||nombre.isBlank()||nombre.length()>120)throw new IllegalArgumentException("Nombre inválido (máximo 120 caracteres).");
  if(telefono==null||!telefono.matches("[0-9]{9}"))throw new IllegalArgumentException("El teléfono debe tener 9 dígitos.");
  if(rol==null)throw new IllegalArgumentException("Rol inválido.");
  if(u.id.equals(actual.id)&&rol!=u.rol)throw new IllegalArgumentException("No puedes cambiar tu propio rol.");
  if(u.rol==Rol.ADMINISTRADOR&&rol!=Rol.ADMINISTRADOR&&u.activo&&dao.countByRolAndActivoTrue(Rol.ADMINISTRADOR)<=1)throw new IllegalArgumentException("Debe quedar al menos un administrador activo.");
  u.nombre=nombre.trim();u.telefono=telefono;u.rol=rol;dao.save(u);
 }

 /** RF51: activa o desactiva una cuenta (un usuario desactivado no puede iniciar sesión). */
 public void cambiarActivo(Long id,boolean activo,Usuario actual){
  Usuario u=dao.findById(id).orElseThrow(()->new IllegalArgumentException("Usuario no encontrado."));
  if(!activo&&u.id.equals(actual.id))throw new IllegalArgumentException("No puedes desactivar tu propia cuenta.");
  if(!activo&&u.rol==Rol.ADMINISTRADOR&&u.activo&&dao.countByRolAndActivoTrue(Rol.ADMINISTRADOR)<=1)throw new IllegalArgumentException("Debe quedar al menos un administrador activo.");
  u.activo=activo;dao.save(u);
 }

 private Usuario guardar(Usuario usuario,String clave){usuario.passwordHash=encoder.encode(clave);return dao.save(usuario);}
}

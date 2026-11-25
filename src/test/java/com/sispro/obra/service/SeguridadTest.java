package com.sispro.obra.service;
import com.sispro.obra.dao.UsuarioDAO;
import com.sispro.obra.model.Usuario;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Pruebas de las correcciones de la auditoría cruzada (H-2 bloqueo y H-3 registro genérico). */
class SeguridadTest {
 private static Usuario usuario(){Usuario u=new Usuario();u.correo="a@b.com";return u;}

 @Test void bloqueaLaCuentaAlQuintoIntentoFallido(){
  UsuarioDAO dao=mock(UsuarioDAO.class);Usuario u=usuario();
  when(dao.bloquearFilaPorCorreo("a@b.com")).thenReturn(Optional.of(u));
  LoginAttemptService s=new LoginAttemptService(dao,mock(AuditoriaService.class));
  for(int i=0;i<4;i++)s.registrarFallo("a@b.com");
  assertNull(u.bloqueadoHasta);
  s.registrarFallo("a@b.com");
  assertNotNull(u.bloqueadoHasta);
  assertTrue(u.bloqueadoHasta.isAfter(LocalDateTime.now().plusMinutes(14)));
 }

 @Test void noExtiendeUnBloqueoVigente(){
  UsuarioDAO dao=mock(UsuarioDAO.class);Usuario u=usuario();
  LocalDateTime hasta=LocalDateTime.now().plusMinutes(10);u.bloqueadoHasta=hasta;
  when(dao.bloquearFilaPorCorreo("a@b.com")).thenReturn(Optional.of(u));
  new LoginAttemptService(dao,mock(AuditoriaService.class)).registrarFallo("a@b.com");
  assertEquals(hasta,u.bloqueadoHasta);
 }

 @Test void unExitoReiniciaElContador(){
  UsuarioDAO dao=mock(UsuarioDAO.class);Usuario u=usuario();u.intentosFallidos=3;
  when(dao.findByCorreoIgnoreCase("a@b.com")).thenReturn(Optional.of(u));
  new LoginAttemptService(dao,mock(AuditoriaService.class)).registrarExito("a@b.com");
  assertEquals(0,u.intentosFallidos);
 }

 @Test void usuarioInexistenteNoRompeNiSeBloquea(){
  UsuarioDAO dao=mock(UsuarioDAO.class);
  when(dao.bloquearFilaPorCorreo("x@y.com")).thenReturn(Optional.empty());
  LoginAttemptService s=new LoginAttemptService(dao,mock(AuditoriaService.class));
  assertDoesNotThrow(()->s.registrarFallo("x@y.com"));
  verify(dao,never()).save(any());
 }

 @Test void rechazaContrasenasDebiles(){
  UsuarioService s=new UsuarioService(mock(UsuarioDAO.class),new BCryptPasswordEncoder());
  assertThrows(IllegalArgumentException.class,()->s.validarClave("abc"));
  assertThrows(IllegalArgumentException.class,()->s.validarClave("todominuscula1"));
  assertDoesNotThrow(()->s.validarClave("Clave1234"));
 }

 @Test void registroPublicoNoRevelaSiElCorreoYaExiste(){
  UsuarioDAO dao=mock(UsuarioDAO.class);
  when(dao.existsByCorreoIgnoreCase("a@b.com")).thenReturn(true);
  UsuarioService s=new UsuarioService(dao,new BCryptPasswordEncoder());
  assertFalse(s.registrarPublico(usuario(),"Clave1234"));
  verify(dao,never()).save(any());
 }
}

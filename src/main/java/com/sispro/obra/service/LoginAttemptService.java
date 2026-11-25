package com.sispro.obra.service;
import com.sispro.obra.dao.UsuarioDAO;
import com.sispro.obra.model.Usuario;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Control de intentos fallidos de inicio de sesión (RF08): 5 fallos -> bloqueo de 15 minutos. */
@Service public class LoginAttemptService {
 public static final int MAX_INTENTOS=5;
 public static final int MINUTOS_BLOQUEO=15;
 private final UsuarioDAO usuarios; private final AuditoriaService auditoria;
 public LoginAttemptService(UsuarioDAO usuarios,AuditoriaService auditoria){this.usuarios=usuarios;this.auditoria=auditoria;}

 private static String recortar(String s){return s.length()>100?s.substring(0,100):s;}

 @Transactional public void registrarFallo(String correo){
  if(correo==null||correo.isBlank())return;
  String c=recortar(correo.trim());
  var encontrado=usuarios.bloquearFilaPorCorreo(c);
  if(encontrado.isEmpty()){auditoria.registrar(c,"LOGIN_FALLIDO","usuario desconocido");return;}
  Usuario u=encontrado.get();
  LocalDateTime ahora=LocalDateTime.now();
  if(u.bloqueadoHasta!=null){
   if(u.bloqueadoHasta.isAfter(ahora))return;      // ya bloqueada: no se extiende el bloqueo
   u.bloqueadoHasta=null;u.intentosFallidos=0;      // bloqueo vencido: se empieza de cero
  }
  u.intentosFallidos++;
  if(u.intentosFallidos>=MAX_INTENTOS){
   u.bloqueadoHasta=ahora.plusMinutes(MINUTOS_BLOQUEO);u.intentosFallidos=0;
   usuarios.save(u);auditoria.registrar(u.correo,"CUENTA_BLOQUEADA",MINUTOS_BLOQUEO+" minutos");
  }else{
   usuarios.save(u);auditoria.registrar(u.correo,"LOGIN_FALLIDO","intento "+u.intentosFallidos+" de "+MAX_INTENTOS);
  }
 }

 @Transactional public void registrarExito(String correo){
  usuarios.findByCorreoIgnoreCase(correo).ifPresent(u->{
   if(u.intentosFallidos!=0||u.bloqueadoHasta!=null){u.intentosFallidos=0;u.bloqueadoHasta=null;usuarios.save(u);}
   auditoria.registrar(u.correo,"LOGIN_OK","");
  });
 }
}

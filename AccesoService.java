package com.sispro.obra.service;
import com.sispro.obra.dao.*;
import com.sispro.obra.model.*;
import java.util.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
@Service public class AccesoService {
 private final UsuarioDAO usuarios; private final ObraDAO obras; private final AsignacionObraDAO asignaciones;
 public AccesoService(UsuarioDAO usuarios,ObraDAO obras,AsignacionObraDAO asignaciones){this.usuarios=usuarios;this.obras=obras;this.asignaciones=asignaciones;}
 public Usuario actual(Authentication a){return usuarios.findByCorreoIgnoreCase(a.getName()).orElseThrow();}
 public boolean esAdmin(Usuario u){return u.rol==Rol.ADMINISTRADOR;}
 public boolean esSupervisor(Usuario u){return u.rol==Rol.SUPERVISOR;}
 public boolean puedeVer(Usuario u,Obra o){return esAdmin(u)||(o.ingenieroResidente!=null&&o.ingenieroResidente.id.equals(u.id))||(u.obraAsignada!=null&&u.obraAsignada.id.equals(o.id))||asignaciones.existsByObraIdAndUsuarioId(o.id,u.id);}
 public void exigirVer(Usuario u,Obra o){if(!puedeVer(u,o))throw new AccessDeniedException("No perteneces a esta obra");}
 public void exigirGestion(Usuario u,Obra o){if(!(esAdmin(u)||(esSupervisor(u)&&puedeVer(u,o))))throw new AccessDeniedException("Sin permiso de gestión");}
 public List<Obra> visibles(Usuario u){if(esAdmin(u))return obras.findAll();return obras.findAll().stream().filter(o->puedeVer(u,o)).toList();}
}

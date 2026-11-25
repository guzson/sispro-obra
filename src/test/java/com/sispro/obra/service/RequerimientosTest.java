package com.sispro.obra.service;
import com.sispro.obra.dao.*;
import com.sispro.obra.model.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Pruebas de las correcciones de la revisión cruzada de requerimientos (RF15-17, RF21, RF24, RF51). */
class RequerimientosTest {
 private static IncidenciaService servicio(IncidenciaDAO dao){return new IncidenciaService(dao,mock(ObraDAO.class));}
 private static Incidencia incidencia(EstadoIncidencia estado,Usuario responsable){Incidencia i=new Incidencia();i.estado=estado;i.responsable=responsable;return i;}
 private static Usuario usuario(Long id,Rol rol){Usuario u=new Usuario();u.id=id;u.rol=rol;u.correo="u"+id+"@x.com";return u;}

 @Test void prioridadAutomaticaPorTipo(){
  assertEquals(Prioridad.ALTA,IncidenciaService.calcularPrioridad(TipoIncidencia.ERROR_EJECUCION,null));
  assertEquals(Prioridad.ALTA,IncidenciaService.calcularPrioridad(TipoIncidencia.REPROCESO,null));
  assertEquals(Prioridad.MEDIA,IncidenciaService.calcularPrioridad(TipoIncidencia.RETRASO,null));
  assertEquals(Prioridad.BAJA,IncidenciaService.calcularPrioridad(TipoIncidencia.OTRA,null));
  assertEquals(Prioridad.MEDIA,IncidenciaService.calcularPrioridad(null,null));
 }

 @Test void prioridadDeFaltaDeMaterialesSegunCantidad(){
  assertEquals(Prioridad.MEDIA,IncidenciaService.calcularPrioridad(TipoIncidencia.FALTA_MATERIALES,10));
  assertEquals(Prioridad.MEDIA,IncidenciaService.calcularPrioridad(TipoIncidencia.FALTA_MATERIALES,20));
  assertEquals(Prioridad.ALTA,IncidenciaService.calcularPrioridad(TipoIncidencia.FALTA_MATERIALES,50));
  assertEquals(Prioridad.CRITICA,IncidenciaService.calcularPrioridad(TipoIncidencia.FALTA_MATERIALES,100));
 }

 @Test void noReasignaIncidenciasResueltasNiAnuladas(){
  assertThrows(IllegalArgumentException.class,()->IncidenciaService.exigirAsignable(incidencia(EstadoIncidencia.RESUELTA,null)));
  assertThrows(IllegalArgumentException.class,()->IncidenciaService.exigirAsignable(incidencia(EstadoIncidencia.ANULADA,null)));
  assertDoesNotThrow(()->IncidenciaService.exigirAsignable(incidencia(EstadoIncidencia.PENDIENTE,null)));
 }

 @Test void noCambiaEstadoSinResponsable(){
  IncidenciaDAO dao=mock(IncidenciaDAO.class);
  when(dao.findById(1L)).thenReturn(Optional.of(incidencia(EstadoIncidencia.PENDIENTE,null)));
  assertThrows(IllegalArgumentException.class,()->servicio(dao).cambiarEstado(1L,EstadoIncidencia.ASIGNADA,null));
  verify(dao,never()).save(any());
 }

 @Test void guardaFechasAlCambiarEstado(){
  IncidenciaDAO dao=mock(IncidenciaDAO.class);Incidencia i=incidencia(EstadoIncidencia.PENDIENTE,usuario(2L,Rol.TRABAJADOR));
  when(dao.findById(1L)).thenReturn(Optional.of(i));
  IncidenciaService s=servicio(dao);
  s.cambiarEstado(1L,EstadoIncidencia.ASIGNADA,null);
  assertNotNull(i.fechaAsignacion);assertNotNull(i.actualizada);assertNull(i.fechaResolucion);
  s.cambiarEstado(1L,EstadoIncidencia.EN_PROCESO,null);
  s.cambiarEstado(1L,EstadoIncidencia.RESUELTA,"Se repuso el material");
  assertNotNull(i.fechaResolucion);assertEquals("Se repuso el material",i.solucion);
 }

 @Test void noDesactivaAlUltimoAdministradorNiASiMismo(){
  UsuarioDAO dao=mock(UsuarioDAO.class);Usuario admin=usuario(1L,Rol.ADMINISTRADOR);Usuario otro=usuario(2L,Rol.ADMINISTRADOR);
  when(dao.findById(1L)).thenReturn(Optional.of(admin));
  when(dao.countByRolAndActivoTrue(Rol.ADMINISTRADOR)).thenReturn(1L);
  UsuarioService s=new UsuarioService(dao,new BCryptPasswordEncoder());
  assertThrows(IllegalArgumentException.class,()->s.cambiarActivo(1L,false,admin));   // a sí mismo
  assertThrows(IllegalArgumentException.class,()->s.cambiarActivo(1L,false,otro));    // último administrador
  assertTrue(admin.activo);
 }

 @Test void desactivaYReactivaUnUsuario(){
  UsuarioDAO dao=mock(UsuarioDAO.class);Usuario admin=usuario(1L,Rol.ADMINISTRADOR);Usuario t=usuario(3L,Rol.TRABAJADOR);
  when(dao.findById(3L)).thenReturn(Optional.of(t));
  UsuarioService s=new UsuarioService(dao,new BCryptPasswordEncoder());
  s.cambiarActivo(3L,false,admin);assertFalse(t.activo);
  s.cambiarActivo(3L,true,admin);assertTrue(t.activo);
 }

 @Test void editaUsuarioYValidaDatos(){
  UsuarioDAO dao=mock(UsuarioDAO.class);Usuario admin=usuario(1L,Rol.ADMINISTRADOR);Usuario t=usuario(3L,Rol.TRABAJADOR);
  when(dao.findById(3L)).thenReturn(Optional.of(t));
  when(dao.findById(1L)).thenReturn(Optional.of(admin));
  UsuarioService s=new UsuarioService(dao,new BCryptPasswordEncoder());
  assertThrows(IllegalArgumentException.class,()->s.actualizar(3L,"Ana","123",Rol.SUPERVISOR,admin));
  s.actualizar(3L,"  Ana Pérez ","987654321",Rol.SUPERVISOR,admin);
  assertEquals("Ana Pérez",t.nombre);assertEquals(Rol.SUPERVISOR,t.rol);
  assertThrows(IllegalArgumentException.class,()->s.actualizar(1L,"Yo","987654321",Rol.TRABAJADOR,admin)); // no cambia su propio rol
 }
}

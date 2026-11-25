package com.sispro.obra.service;
import com.sispro.obra.dao.*;
import com.sispro.obra.model.*;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service public class IncidenciaService {
 private final IncidenciaDAO dao;private final ObraDAO obras;
 public IncidenciaService(IncidenciaDAO dao,ObraDAO obras){this.dao=dao;this.obras=obras;}

 /**
  * Regla de negocio de prioridad automática (RF15, RF16, RF17). Puntaje base por tipo:
  * ERROR_EJECUCION 4, REPROCESO 4, RETRASO 3, FALTA_MATERIALES 2, OTRA 1. En falta de materiales suma
  * +1 (20 o más unidades), +2 (50 o más) o +3 (100 o más). Puntaje: 5+ CRITICA, 4 ALTA, 2-3 MEDIA, 1 BAJA.
  * Sin tipo no hay datos suficientes: prioridad provisional MEDIA. Quien asigna puede ajustarla a mano.
  */
 public static Prioridad calcularPrioridad(TipoIncidencia tipo,Integer cantidad){
  if(tipo==null)return Prioridad.MEDIA;
  int p=switch(tipo){case ERROR_EJECUCION,REPROCESO->4;case RETRASO->3;case FALTA_MATERIALES->2;case OTRA->1;};
  if(tipo==TipoIncidencia.FALTA_MATERIALES&&cantidad!=null){if(cantidad>=100)p+=3;else if(cantidad>=50)p+=2;else if(cantidad>=20)p+=1;}
  if(p>=5)return Prioridad.CRITICA;
  if(p==4)return Prioridad.ALTA;
  if(p>=2)return Prioridad.MEDIA;
  return Prioridad.BAJA;
 }

 /** RF21: una incidencia resuelta o anulada no se reasigna. */
 public static void exigirAsignable(Incidencia i){if(i.estado==EstadoIncidencia.RESUELTA||i.estado==EstadoIncidencia.ANULADA)throw new IllegalArgumentException("No se puede reasignar una incidencia resuelta o anulada.");}

 @Transactional public Incidencia crear(Incidencia i,Long obraId){Obra obra=obras.findById(obraId).orElseThrow();if(obra.estado!=EstadoObra.ACTIVA)throw new IllegalArgumentException("Solo las obras activas admiten incidencias.");if(i.titulo==null||i.titulo.isBlank()||i.descripcion==null||i.descripcion.isBlank()||i.etapa==null||i.etapa.isBlank())throw new IllegalArgumentException("Complete título, descripción y etapa.");if(i.tipo==TipoIncidencia.FALTA_MATERIALES&&(i.cantidadMaterial==null||i.cantidadMaterial<=0))throw new IllegalArgumentException("La cantidad de materiales debe ser positiva.");if(i.tipo!=TipoIncidencia.FALTA_MATERIALES)i.cantidadMaterial=null;i.obra=obra;i.estado=EstadoIncidencia.PENDIENTE;i.prioridad=calcularPrioridad(i.tipo,i.cantidadMaterial);i.creada=LocalDateTime.now();return dao.save(i);}

 public void validarTransicion(EstadoIncidencia desde,EstadoIncidencia hasta,String solucion){boolean valida=switch(desde){case PENDIENTE -> hasta==EstadoIncidencia.ASIGNADA;case ASIGNADA -> hasta==EstadoIncidencia.EN_PROCESO;case EN_PROCESO -> hasta==EstadoIncidencia.RESUELTA;default -> false;};if(!valida)throw new IllegalArgumentException("Transición de estado no permitida.");if(hasta==EstadoIncidencia.RESUELTA&&(solucion==null||solucion.isBlank()))throw new IllegalArgumentException("Debe describir la solución aplicada.");}

 /** RF24: valida la secuencia, exige responsable y guarda las fechas usadas por las métricas (RF40). */
 @Transactional public void cambiarEstado(Long id,EstadoIncidencia nuevo,String solucion){
  Incidencia i=dao.findById(id).orElseThrow();
  validarTransicion(i.estado,nuevo,solucion);
  if(i.responsable==null)throw new IllegalArgumentException("Asigne un responsable antes de cambiar el estado.");
  LocalDateTime ahora=LocalDateTime.now();
  i.estado=nuevo;i.actualizada=ahora;
  if(nuevo==EstadoIncidencia.ASIGNADA&&i.fechaAsignacion==null)i.fechaAsignacion=ahora;
  if(nuevo==EstadoIncidencia.RESUELTA){i.solucion=solucion;i.fechaResolucion=ahora;}
  dao.save(i);
 }
}

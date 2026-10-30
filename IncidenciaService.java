package com.sispro.obra.service;
import com.sispro.obra.dao.*;
import com.sispro.obra.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service public class IncidenciaService {
 private final IncidenciaDAO dao;private final ObraDAO obras;
 public IncidenciaService(IncidenciaDAO dao,ObraDAO obras){this.dao=dao;this.obras=obras;}
 @Transactional public Incidencia crear(Incidencia i,Long obraId){Obra obra=obras.findById(obraId).orElseThrow();if(obra.estado!=EstadoObra.ACTIVA)throw new IllegalArgumentException("Solo las obras activas admiten incidencias.");if(i.titulo==null||i.titulo.isBlank()||i.descripcion==null||i.descripcion.isBlank()||i.etapa==null||i.etapa.isBlank())throw new IllegalArgumentException("Complete título, descripción y etapa.");if(i.tipo==TipoIncidencia.FALTA_MATERIALES&&(i.cantidadMaterial==null||i.cantidadMaterial<=0))throw new IllegalArgumentException("La cantidad de materiales debe ser positiva.");i.obra=obra;i.estado=EstadoIncidencia.PENDIENTE;return dao.save(i);}
 public void validarTransicion(EstadoIncidencia desde,EstadoIncidencia hasta,String solucion){boolean valida=switch(desde){case PENDIENTE -> hasta==EstadoIncidencia.ASIGNADA;case ASIGNADA -> hasta==EstadoIncidencia.EN_PROCESO;case EN_PROCESO -> hasta==EstadoIncidencia.RESUELTA;default -> false;};if(!valida)throw new IllegalArgumentException("Transición de estado no permitida.");if(hasta==EstadoIncidencia.RESUELTA&&(solucion==null||solucion.isBlank()))throw new IllegalArgumentException("Debe describir la solución aplicada.");}
 @Transactional public void cambiarEstado(Long id,EstadoIncidencia nuevo,String solucion){Incidencia i=dao.findById(id).orElseThrow();validarTransicion(i.estado,nuevo,solucion);i.estado=nuevo;if(nuevo==EstadoIncidencia.RESUELTA)i.solucion=solucion;dao.save(i);}
}

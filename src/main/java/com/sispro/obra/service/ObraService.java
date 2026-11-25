package com.sispro.obra.service;
import com.sispro.obra.dao.*;
import com.sispro.obra.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service public class ObraService {
 private final ObraDAO obras;private final IncidenciaDAO incidencias;
 public ObraService(ObraDAO obras,IncidenciaDAO incidencias){this.obras=obras;this.incidencias=incidencias;}
 public void validarFechas(Obra obra){if(obra.fechaInicio==null||obra.fechaFin==null||!obra.fechaFin.isAfter(obra.fechaInicio)) throw new IllegalArgumentException("La fecha de fin debe ser posterior a la de inicio.");}
 @Transactional public Obra guardar(Obra obra){validarFechas(obra); if(obra.id!=null){Obra actual=obras.findById(obra.id).orElseThrow();if(actual.estado==EstadoObra.CERRADA)throw new IllegalArgumentException("No se puede editar una obra cerrada.");} obra.estado=obra.ingenieroResidente==null?EstadoObra.BORRADOR:EstadoObra.ACTIVA;return obras.save(obra);}
 @Transactional public void cerrar(Long id){Obra obra=obras.findById(id).orElseThrow();boolean hayPendientes=incidencias.findByObraIdOrderByCreadaDesc(id).stream().anyMatch(i->i.estado!=EstadoIncidencia.RESUELTA&&i.estado!=EstadoIncidencia.ANULADA);if(hayPendientes)throw new IllegalArgumentException("Existen incidencias sin resolver.");obra.estado=EstadoObra.CERRADA;obras.save(obra);}
}

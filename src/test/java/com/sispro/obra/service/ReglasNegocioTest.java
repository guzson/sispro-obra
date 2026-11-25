package com.sispro.obra.service;
import com.sispro.obra.dao.*;
import com.sispro.obra.model.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class ReglasNegocioTest {
 @Test void rechazaFechaFinalAnterior(){ObraService service=new ObraService(mock(ObraDAO.class),mock(IncidenciaDAO.class));Obra obra=new Obra();obra.fechaInicio=LocalDate.of(2026,10,20);obra.fechaFin=LocalDate.of(2026,10,10);assertThrows(IllegalArgumentException.class,()->service.validarFechas(obra));}
 @Test void aceptaFechasValidas(){ObraService service=new ObraService(mock(ObraDAO.class),mock(IncidenciaDAO.class));Obra obra=new Obra();obra.fechaInicio=LocalDate.of(2026,10,20);obra.fechaFin=LocalDate.of(2026,11,20);assertDoesNotThrow(()->service.validarFechas(obra));}
 @Test void noPermiteResolverDesdePendiente(){IncidenciaService service=new IncidenciaService(mock(IncidenciaDAO.class),mock(ObraDAO.class));assertThrows(IllegalArgumentException.class,()->service.validarTransicion(EstadoIncidencia.PENDIENTE,EstadoIncidencia.RESUELTA,"hecho"));}
 @Test void exigeSolucionAlResolver(){IncidenciaService service=new IncidenciaService(mock(IncidenciaDAO.class),mock(ObraDAO.class));assertThrows(IllegalArgumentException.class,()->service.validarTransicion(EstadoIncidencia.EN_PROCESO,EstadoIncidencia.RESUELTA,""));}
 @Test void permiteFlujoCorrecto(){IncidenciaService service=new IncidenciaService(mock(IncidenciaDAO.class),mock(ObraDAO.class));assertDoesNotThrow(()->service.validarTransicion(EstadoIncidencia.ASIGNADA,EstadoIncidencia.EN_PROCESO,null));}
}

package com.sispro.obra.config;

import com.sispro.obra.dao.*;
import com.sispro.obra.model.*;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

/** Solo datos de demostración: deshabilitar antes de publicar. */
@Configuration
public class AdminInitializer {
    @Bean
    CommandLineRunner demoData(UsuarioDAO usuarios, ObraDAO obras, IncidenciaDAO incidencias,
            EtapaDAO etapas, TareaDAO tareas, AsignacionObraDAO asignaciones, PasswordEncoder encoder, @Value("${sispro.demo.enabled:true}") boolean demoEnabled) {
        return args -> {
            if (!demoEnabled) return;
            Usuario admin = crearSiNoExiste(usuarios, encoder, "Administrador Demo", "admin@sispro.local", "Admin1234", Rol.ADMINISTRADOR);
            Usuario supervisor = crearSiNoExiste(usuarios, encoder, "Supervisor Demo", "supervisor@sispro.local", "Supervisor123", Rol.SUPERVISOR);
            Usuario trabajador = crearSiNoExiste(usuarios, encoder, "Trabajador Demo", "trabajador@sispro.local", "Trabajador123", Rol.TRABAJADOR);
            // Datos de prueba, solo si todavía no hay obras.
            if (obras.count() == 0) {
                Obra obra = new Obra();
                obra.nombre = "Residencial Los Pinos";
                obra.direccion = "San Juan de Miraflores, Lima";
                obra.fechaInicio = LocalDate.of(2026, 10, 1);
                obra.fechaFin = LocalDate.of(2027, 6, 30);
                obra.ingenieroResidente = supervisor;
                obra.estado = EstadoObra.ACTIVA;
                obra = obras.save(obra);
                AsignacionObra x = new AsignacionObra(); x.obra=obra; x.usuario=trabajador; asignaciones.save(x);
                Etapa etapa=new Etapa();etapa.obra=obra;etapa.nombre="Cimentación";etapa.inicio=LocalDate.of(2026,10,1);etapa.fin=LocalDate.of(2026,12,15);etapa=etapas.save(etapa);
                Tarea tarea=new Tarea();tarea.obra=obra;tarea.etapa=etapa;tarea.nombre="Preparación de terreno";tarea.descripcion="Nivelación y trazado inicial";tarea.inicio=LocalDate.of(2026,10,2);tarea.fin=LocalDate.of(2026,11,30);tarea.responsable=trabajador;tarea.avance=35;tareas.save(tarea);
                if (incidencias.count() == 0) {
                    Incidencia incidencia = new Incidencia();
                    incidencia.titulo = "Falta de cemento";
                    incidencia.descripcion = "Se requiere reposición de cemento para continuar la cimentación.";
                    incidencia.etapa = "Cimentación";
                    incidencia.tipo = TipoIncidencia.FALTA_MATERIALES;
                    incidencia.cantidadMaterial = 20;
                    incidencia.obra = obra;
                    incidencias.save(incidencia);
                }
            }
        };
    }

    private static Usuario crearSiNoExiste(UsuarioDAO dao, PasswordEncoder encoder,
            String nombre, String correo, String clave, Rol rol) {
        return dao.findByCorreoIgnoreCase(correo).orElseGet(() -> {
            Usuario usuario = new Usuario();
            usuario.nombre = nombre;
            usuario.correo = correo;
            usuario.telefono = "999999999";
            usuario.rol = rol;
            usuario.passwordHash = encoder.encode(clave);
            return dao.save(usuario);
        });
    }
}

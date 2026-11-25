package com.sispro.obra.model;
import jakarta.persistence.*;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
@Entity @Table(name="tareas") public class Tarea {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @ManyToOne(optional=false) public Obra obra;
 @ManyToOne public Etapa etapa;
 @ManyToOne public Usuario responsable;
 @Column(nullable=false) public String nombre;
 @Column(length=1000) public String descripcion;
 @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) public LocalDate inicio;
 @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) public LocalDate fin;
 public Integer avance=0;
}

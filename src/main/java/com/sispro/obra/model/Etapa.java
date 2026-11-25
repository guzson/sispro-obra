package com.sispro.obra.model;
import jakarta.persistence.*;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
@Entity @Table(name="etapas") public class Etapa {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @ManyToOne(optional=false) public Obra obra;
 @Column(nullable=false) public String nombre;
 @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) public LocalDate inicio;
 @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) public LocalDate fin;
}

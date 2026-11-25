package com.sispro.obra.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="comentarios") public class Comentario {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @ManyToOne(optional=false) public Incidencia incidencia;
 @ManyToOne(optional=false) public Usuario autor;
 @Column(nullable=false,length=1500) public String texto;
 public LocalDateTime creada=LocalDateTime.now();
}

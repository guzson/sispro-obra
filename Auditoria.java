package com.sispro.obra.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="auditoria") public class Auditoria {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 public String usuario,accion,referencia;
 public LocalDateTime fecha=LocalDateTime.now();
}

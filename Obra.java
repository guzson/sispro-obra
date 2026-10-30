package com.sispro.obra.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
@Entity @Table(name="obras") public class Obra {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @NotBlank public String nombre;
 @NotBlank public String direccion;
 public Double latitud, longitud;
 @NotNull @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) public LocalDate fechaInicio;
 @NotNull @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) public LocalDate fechaFin;
 @ManyToOne(fetch=FetchType.EAGER) public Usuario ingenieroResidente;
 @Enumerated(EnumType.STRING) public EstadoObra estado=EstadoObra.BORRADOR;
 public Obra(){}
}

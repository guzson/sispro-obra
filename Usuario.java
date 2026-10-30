package com.sispro.obra.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
@Entity @Table(name="usuarios",uniqueConstraints=@UniqueConstraint(columnNames="correo"))
public class Usuario {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @NotBlank public String nombre;
 @Email @NotBlank @Column(nullable=false,unique=true) public String correo;
 @Pattern(regexp="[0-9]{9}",message="El teléfono debe tener 9 dígitos") public String telefono;
 @Enumerated(EnumType.STRING) @Column(nullable=false) public Rol rol=Rol.TRABAJADOR;
 @Column(nullable=false) public String passwordHash;
 @ManyToOne(fetch=FetchType.EAGER) public Obra obraAsignada;
 @Column(nullable=false, columnDefinition="integer default 0") public int intentosFallidos=0;
 public LocalDateTime bloqueadoHasta;
 public Usuario(){}
}

package com.sispro.obra.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
@Entity @Table(name="incidencias") public class Incidencia {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @NotBlank public String titulo;
 @NotBlank @Column(length=2000) public String descripcion;
 @NotNull @Enumerated(EnumType.STRING) public TipoIncidencia tipo=TipoIncidencia.ERROR_EJECUCION;
 @NotBlank public String etapa;
 public Integer cantidadMaterial;
 @Enumerated(EnumType.STRING) public Prioridad prioridad=Prioridad.MEDIA;
 @ManyToOne public Usuario responsable;
 @Column(length=500) public String motivoAnulacion;
 @ManyToOne(optional=false) public Obra obra;
 @Enumerated(EnumType.STRING) public EstadoIncidencia estado=EstadoIncidencia.PENDIENTE;
 @Column(length=2000) public String solucion;
 public LocalDateTime creada=LocalDateTime.now();
}

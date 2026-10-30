package com.sispro.obra.model;
import jakarta.persistence.*;
@Entity @Table(name="asignaciones_obra",uniqueConstraints=@UniqueConstraint(columnNames={"obra_id","usuario_id"}))
public class AsignacionObra {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @ManyToOne(optional=false) public Obra obra;
 @ManyToOne(optional=false) public Usuario usuario;
}

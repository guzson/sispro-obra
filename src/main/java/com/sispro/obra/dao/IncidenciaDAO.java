package com.sispro.obra.dao;
import com.sispro.obra.model.Incidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface IncidenciaDAO extends JpaRepository<Incidencia,Long> {
 List<Incidencia> findByObraIdOrderByCreadaDesc(Long obraId);
}

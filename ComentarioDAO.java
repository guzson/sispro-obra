package com.sispro.obra.dao;
import com.sispro.obra.model.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface ComentarioDAO extends JpaRepository<Comentario,Long> { List<Comentario> findByIncidenciaIdOrderByCreadaAsc(Long id); }

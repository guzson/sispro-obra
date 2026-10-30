package com.sispro.obra.dao;
import com.sispro.obra.model.Tarea;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface TareaDAO extends JpaRepository<Tarea,Long> { List<Tarea> findByObraIdOrderByInicioAsc(Long id); }

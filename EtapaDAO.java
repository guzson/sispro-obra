package com.sispro.obra.dao;
import com.sispro.obra.model.Etapa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface EtapaDAO extends JpaRepository<Etapa,Long> { List<Etapa> findByObraIdOrderByInicioAsc(Long id); }

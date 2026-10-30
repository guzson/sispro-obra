package com.sispro.obra.dao;
import com.sispro.obra.model.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface AuditoriaDAO extends JpaRepository<Auditoria,Long> { List<Auditoria> findTop100ByOrderByFechaDesc(); }

package com.sispro.obra.dao;
import com.sispro.obra.model.AsignacionObra;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface AsignacionObraDAO extends JpaRepository<AsignacionObra,Long> { boolean existsByObraIdAndUsuarioId(Long obraId,Long usuarioId); List<AsignacionObra> findByUsuarioId(Long userId); List<AsignacionObra> findByObraId(Long obraId); void deleteByObraIdAndUsuarioId(Long obraId,Long userId); }

package com.sispro.obra.dao;
import com.sispro.obra.model.Obra;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface ObraDAO extends JpaRepository<Obra,Long> {
}

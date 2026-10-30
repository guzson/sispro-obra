package com.sispro.obra.dao;
import com.sispro.obra.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface UsuarioDAO extends JpaRepository<Usuario,Long> {
 @Lock(LockModeType.PESSIMISTIC_WRITE)
 @Query("select u from Usuario u where lower(u.correo)=lower(:correo)")
 Optional<Usuario> bloquearFilaPorCorreo(@Param("correo") String correo);
 Optional<Usuario> findByCorreoIgnoreCase(String correo); boolean existsByCorreoIgnoreCase(String correo);
}

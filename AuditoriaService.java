package com.sispro.obra.service;
import com.sispro.obra.dao.AuditoriaDAO;
import com.sispro.obra.model.Auditoria;
import org.springframework.stereotype.Service;
@Service public class AuditoriaService {
 private final AuditoriaDAO dao;
 public AuditoriaService(AuditoriaDAO dao){this.dao=dao;}
 public void registrar(String usuario,String accion,String referencia){Auditoria x=new Auditoria();x.usuario=usuario;x.accion=accion;x.referencia=referencia;dao.save(x);}
}

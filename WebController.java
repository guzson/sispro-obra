package com.sispro.obra.controller;
import com.sispro.obra.dao.*;
import com.sispro.obra.model.*;
import com.sispro.obra.service.*;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
@Controller public class WebController {
 private final AccesoService acceso;private final ObraDAO obras;private final UsuarioDAO usuarios;private final IncidenciaDAO incidencias;private final ObraService obraService;private final UsuarioService usuarioService;private final IncidenciaService incidenciaService;
 public WebController(ObraDAO obras,UsuarioDAO usuarios,IncidenciaDAO incidencias,ObraService obraService,UsuarioService usuarioService,IncidenciaService incidenciaService,AccesoService acceso){this.acceso=acceso;this.obras=obras;this.usuarios=usuarios;this.incidencias=incidencias;this.obraService=obraService;this.usuarioService=usuarioService;this.incidenciaService=incidenciaService;}
 @GetMapping("/login")String login(){return "login";}
 @GetMapping("/registro")String registro(){return "registro";}
 @PostMapping("/registro")String registrarPublico(@RequestParam String nombre,@RequestParam String correo,@RequestParam String telefono,@RequestParam String clave,RedirectAttributes r){
     try {
         if(nombre==null||nombre.isBlank()||correo==null||!correo.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")||!telefono.matches("[0-9]{9}"))throw new IllegalArgumentException("Revisa nombre, correo y teléfono (9 dígitos).");
         Usuario u=new Usuario();u.nombre=nombre.trim();u.correo=correo.trim();u.telefono=telefono;u.rol=Rol.TRABAJADOR;
         usuarioService.registrar(u,clave);
         r.addFlashAttribute("ok","Registro completado. Ahora puedes iniciar sesión.");return "redirect:/login?registrado";
     }catch(Exception e){r.addFlashAttribute("error",e.getMessage());return "redirect:/registro";}
 }

 @GetMapping("/")String inicio(Model m,Authentication a){m.addAttribute("nombre",a.getName());m.addAttribute("obras",acceso.visibles(acceso.actual(a)).size());m.addAttribute("usuarios",usuarios.count());m.addAttribute("incidencias",incidencias.findAll().stream().filter(i->acceso.puedeVer(acceso.actual(a),i.obra)).count());m.addAttribute("recientes",incidencias.findAll().stream().filter(i->acceso.puedeVer(acceso.actual(a),i.obra)).limit(5).toList());m.addAttribute("obrasLista",acceso.visibles(acceso.actual(a)));return "inicio";}
 @GetMapping("/usuarios")String usuarios(Model m){m.addAttribute("usuarios",usuarios.findAll());return "usuarios";}
 @GetMapping("/usuarios/nuevo")String usuarioNuevo(Model m){m.addAttribute("usuario",new Usuario());m.addAttribute("obras",obras.findAll());m.addAttribute("roles",Rol.values());return "usuario-form";}
 @PostMapping("/usuarios/guardar")String guardarUsuario(@ModelAttribute Usuario usuario,@RequestParam String clave,@RequestParam(required=false) Long obraId,RedirectAttributes r){try{if(obraId!=null)usuario.obraAsignada=obras.findById(obraId).orElseThrow();usuarioService.registrar(usuario,clave);r.addFlashAttribute("ok","Usuario registrado correctamente.");return "redirect:/usuarios";}catch(Exception e){r.addFlashAttribute("error",e.getMessage());return "redirect:/usuarios/nuevo";}}
 @GetMapping("/obras")String obras(Model m,Authentication a){m.addAttribute("obras",acceso.visibles(acceso.actual(a)));return "obras";}
 @GetMapping("/obras/nueva")String obraNueva(Model m){m.addAttribute("obra",new Obra());m.addAttribute("usuarios",usuarios.findAll());return "obra-form";}
 @GetMapping("/obras/{id}/editar")String obraEditar(@PathVariable Long id,Model m){m.addAttribute("obra",obras.findById(id).orElseThrow());m.addAttribute("usuarios",usuarios.findAll());return "obra-form";}
 @PostMapping("/obras/guardar")
 String guardarObra(@ModelAttribute("obra") @Valid Obra obra,
                   BindingResult resultado,
                   @RequestParam(required=false) Long ingenieroId,
                   Model modelo, RedirectAttributes r) {
     if (resultado.hasErrors()) {
         modelo.addAttribute("usuarios", usuarios.findAll());
         modelo.addAttribute("error", "Revisa los campos obligatorios y el formato de las fechas.");
         return "obra-form";
     }
     try {
         obra.ingenieroResidente = ingenieroId == null ? null
                 : usuarios.findById(ingenieroId).orElseThrow(() ->
                         new IllegalArgumentException("Ingeniero no encontrado"));
         obraService.guardar(obra);
         r.addFlashAttribute("ok", "Obra guardada correctamente.");
         return "redirect:/obras";
     } catch (RuntimeException e) {
         modelo.addAttribute("usuarios", usuarios.findAll());
         modelo.addAttribute("error", mensaje(e));
         return "obra-form";
     }
 }
 @PostMapping("/obras/{id}/cerrar")String cerrarObra(@PathVariable Long id,RedirectAttributes r){try{obraService.cerrar(id);r.addFlashAttribute("ok","Obra cerrada.");}catch(Exception e){r.addFlashAttribute("error",e.getMessage());}return "redirect:/obras";}
 @GetMapping("/incidencias")String incidencias(Model m,Authentication a){m.addAttribute("incidencias",incidencias.findAll().stream().filter(i->acceso.puedeVer(acceso.actual(a),i.obra)).toList());return "incidencias";}
 @GetMapping("/incidencias/nueva")String incidenciaNueva(Model m,Authentication a){m.addAttribute("incidencia",new Incidencia());m.addAttribute("obras",acceso.visibles(acceso.actual(a)));m.addAttribute("tipos",TipoIncidencia.values());return "incidencia-form";}
 @PostMapping("/incidencias/guardar")
 String guardarIncidencia(@ModelAttribute("incidencia") @Valid Incidencia incidencia,
                         BindingResult resultado,
                         @RequestParam(required=false) Long obraId,
                         Model modelo, RedirectAttributes r, Authentication a) {
     if (resultado.hasErrors()) {
         rellenarIncidencia(modelo, a);
         modelo.addAttribute("error", "Revisa los campos obligatorios y los valores numéricos.");
         return "incidencia-form";
     }
     try {
         if (obraId == null) throw new IllegalArgumentException("Selecciona una obra activa.");
         Obra obra = obras.findById(obraId).orElseThrow(() ->
                 new IllegalArgumentException("La obra seleccionada no existe."));
         acceso.exigirVer(acceso.actual(a), obra);
         incidenciaService.crear(incidencia, obraId);
         r.addFlashAttribute("ok", "Incidencia registrada.");
         return "redirect:/incidencias";
     } catch (RuntimeException e) {
         rellenarIncidencia(modelo, a);
         modelo.addAttribute("error", mensaje(e));
         modelo.addAttribute("obraSeleccionada", obraId);
         return "incidencia-form";
     }
 }
 private static String mensaje(RuntimeException e) {
     if (e instanceof IllegalArgumentException || e instanceof org.springframework.security.access.AccessDeniedException) return e.getMessage();
     Throwable causa = org.springframework.core.NestedExceptionUtils.getMostSpecificCause(e);
     return "No se pudo guardar: " + (causa.getMessage() == null ? causa.getClass().getSimpleName() : causa.getMessage());
 }
 private void rellenarIncidencia(Model modelo, Authentication a) {
     modelo.addAttribute("obras", acceso.visibles(acceso.actual(a)));
     modelo.addAttribute("tipos", TipoIncidencia.values());
 }
 @PostMapping("/incidencias/{id}/estado")String cambiarEstado(@PathVariable Long id,@RequestParam EstadoIncidencia estado,@RequestParam(required=false) String solucion,RedirectAttributes r,Authentication a){try{acceso.exigirGestion(acceso.actual(a),incidencias.findById(id).orElseThrow().obra);incidenciaService.cambiarEstado(id,estado,solucion);r.addFlashAttribute("ok","Estado actualizado.");}catch(Exception e){r.addFlashAttribute("error",e.getMessage());}return "redirect:/incidencias";}
 @GetMapping("/perfil")String perfil(Authentication a,Model m){m.addAttribute("usuario",usuarios.findByCorreoIgnoreCase(a.getName()).orElseThrow());return "perfil";}
 @PostMapping("/perfil")String guardarPerfil(Authentication a,@RequestParam String nombre,@RequestParam String telefono,RedirectAttributes r){if(!telefono.matches("[0-9]{9}")){r.addFlashAttribute("error","Teléfono inválido: ingrese 9 dígitos.");return "redirect:/perfil";}var u=usuarios.findByCorreoIgnoreCase(a.getName()).orElseThrow();u.nombre=nombre;u.telefono=telefono;usuarios.save(u);r.addFlashAttribute("ok","Perfil actualizado.");return "redirect:/perfil";}
}

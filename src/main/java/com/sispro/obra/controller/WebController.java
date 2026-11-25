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
 private final AccesoService acceso;private final ObraDAO obras;private final UsuarioDAO usuarios;private final IncidenciaDAO incidencias;private final ObraService obraService;private final UsuarioService usuarioService;private final IncidenciaService incidenciaService;private final AuditoriaService audit;
 public WebController(ObraDAO obras,UsuarioDAO usuarios,IncidenciaDAO incidencias,ObraService obraService,UsuarioService usuarioService,IncidenciaService incidenciaService,AccesoService acceso,AuditoriaService audit){this.audit=audit;this.acceso=acceso;this.obras=obras;this.usuarios=usuarios;this.incidencias=incidencias;this.obraService=obraService;this.usuarioService=usuarioService;this.incidenciaService=incidenciaService;}
 @GetMapping("/login")String login(){return "login";}
 @GetMapping("/registro")String registro(){return "registro";}
 @PostMapping("/registro")String registrarPublico(@RequestParam String nombre,@RequestParam String correo,@RequestParam String telefono,@RequestParam String clave,RedirectAttributes r){
     try {
         if(nombre==null||nombre.isBlank()||correo==null||!correo.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")||!telefono.matches("[0-9]{9}"))throw new IllegalArgumentException("Revisa nombre, correo y teléfono (9 dígitos).");
         Usuario u=new Usuario();u.nombre=nombre.trim();u.correo=correo.trim();u.telefono=telefono;u.rol=Rol.TRABAJADOR;
         usuarioService.registrarPublico(u,clave);
         return "redirect:/login?registrado";
     }catch(Exception e){r.addFlashAttribute("error",mensaje(e));return "redirect:/registro";}
 }

 @GetMapping("/")String inicio(Model m,Authentication a){Usuario u=acceso.actual(a);java.util.List<Incidencia> visibles=incidencias.findAll().stream().filter(i->acceso.puedeVer(u,i.obra)).sorted(java.util.Comparator.comparing((Incidencia i)->i.creada,java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder()))).toList();java.util.List<Obra> activas=acceso.visibles(u).stream().filter(o->o.estado!=EstadoObra.CERRADA).toList();m.addAttribute("nombre",u.nombre);m.addAttribute("obras",activas.size());m.addAttribute("incidencias",visibles.size());m.addAttribute("recientes",visibles.stream().limit(5).toList());m.addAttribute("obrasLista",activas);if(acceso.esAdmin(u))m.addAttribute("usuarios",usuarios.count());return "inicio";}
 @GetMapping("/usuarios")String usuarios(Model m){m.addAttribute("usuarios",usuarios.findAll());return "usuarios";}
 @GetMapping("/usuarios/nuevo")String usuarioNuevo(Model m){m.addAttribute("usuario",new Usuario());m.addAttribute("obras",obras.findAll());m.addAttribute("roles",Rol.values());return "usuario-form";}
 @PostMapping("/usuarios/guardar")String guardarUsuario(@ModelAttribute Usuario usuario,@RequestParam String clave,@RequestParam(required=false) Long obraId,RedirectAttributes r){try{if(obraId!=null)usuario.obraAsignada=obras.findById(obraId).orElseThrow();usuarioService.registrar(usuario,clave);r.addFlashAttribute("ok","Usuario registrado correctamente.");return "redirect:/usuarios";}catch(Exception e){r.addFlashAttribute("error",mensaje(e));return "redirect:/usuarios/nuevo";}}
 @GetMapping("/obras")String obras(Model m,Authentication a,@RequestParam(defaultValue="false") boolean archivadas){m.addAttribute("obras",acceso.visibles(acceso.actual(a)).stream().filter(o->archivadas==(o.estado==EstadoObra.CERRADA)).toList());m.addAttribute("archivadas",archivadas);return "obras";}
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
 @PostMapping("/obras/{id}/cerrar")String cerrarObra(@PathVariable Long id,RedirectAttributes r){try{obraService.cerrar(id);r.addFlashAttribute("ok","Obra cerrada.");}catch(Exception e){r.addFlashAttribute("error",mensaje(e));}return "redirect:/obras";}
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
         incidencia.registradaPor = acceso.actual(a);
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
 private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(WebController.class);
 /** Mensajes de negocio se muestran; cualquier otro error va solo al log del servidor (H-4). */
 private static String mensaje(Throwable e) {
     if (e instanceof IllegalArgumentException || e instanceof org.springframework.security.access.AccessDeniedException) return e.getMessage();
     LOG.error("Error al procesar la solicitud", e);
     return "No se pudo completar la operación. Intenta nuevamente.";
 }
 private void rellenarIncidencia(Model modelo, Authentication a) {
     modelo.addAttribute("obras", acceso.visibles(acceso.actual(a)));
     modelo.addAttribute("tipos", TipoIncidencia.values());
 }
 @PostMapping("/incidencias/{id}/estado")String cambiarEstado(@PathVariable Long id,@RequestParam EstadoIncidencia estado,@RequestParam(required=false) String solucion,RedirectAttributes r,Authentication a){try{Incidencia inc=incidencias.findById(id).orElseThrow();Usuario u=acceso.actual(a);boolean esResponsable=inc.responsable!=null&&inc.responsable.id.equals(u.id);if(!esResponsable)acceso.exigirGestion(u,inc.obra);acceso.exigirVer(u,inc.obra);acceso.exigirObraAbierta(inc.obra);incidenciaService.cambiarEstado(id,estado,solucion);audit.registrar(a.getName(),"CAMBIAR_ESTADO","Incidencia "+id+" -> "+estado);r.addFlashAttribute("ok","Estado actualizado.");}catch(Exception e){r.addFlashAttribute("error",mensaje(e));}return "redirect:/incidencias";}
 @GetMapping("/usuarios/{id}/editar")String usuarioEditar(@PathVariable Long id,Model m){m.addAttribute("usuario",usuarios.findById(id).orElseThrow());m.addAttribute("roles",Rol.values());return "usuario-editar";}
 @PostMapping("/usuarios/{id}/actualizar")String actualizarUsuario(@PathVariable Long id,@RequestParam String nombre,@RequestParam String telefono,@RequestParam Rol rol,Authentication a,RedirectAttributes r){try{usuarioService.actualizar(id,nombre,telefono,rol,acceso.actual(a));audit.registrar(a.getName(),"EDITAR_USUARIO","Usuario "+id);r.addFlashAttribute("ok","Usuario actualizado.");return "redirect:/usuarios";}catch(Exception e){r.addFlashAttribute("error",mensaje(e));return "redirect:/usuarios/"+id+"/editar";}}
 @PostMapping("/usuarios/{id}/estado")String estadoUsuario(@PathVariable Long id,@RequestParam boolean activo,Authentication a,RedirectAttributes r){try{usuarioService.cambiarActivo(id,activo,acceso.actual(a));audit.registrar(a.getName(),activo?"ACTIVAR_USUARIO":"DESACTIVAR_USUARIO","Usuario "+id);r.addFlashAttribute("ok",activo?"Usuario activado.":"Usuario desactivado.");}catch(Exception e){r.addFlashAttribute("error",mensaje(e));}return "redirect:/usuarios";}
 @GetMapping("/perfil")String perfil(Authentication a,Model m){m.addAttribute("usuario",usuarios.findByCorreoIgnoreCase(a.getName()).orElseThrow());return "perfil";}
 @PostMapping("/perfil")String guardarPerfil(Authentication a,@RequestParam String nombre,@RequestParam String telefono,RedirectAttributes r){if(!telefono.matches("[0-9]{9}")){r.addFlashAttribute("error","Teléfono inválido: ingrese 9 dígitos.");return "redirect:/perfil";}if(nombre==null||nombre.isBlank()||nombre.length()>120){r.addFlashAttribute("error","Nombre inválido (máximo 120 caracteres).");return "redirect:/perfil";}var u=usuarios.findByCorreoIgnoreCase(a.getName()).orElseThrow();u.nombre=nombre.trim();u.telefono=telefono;usuarios.save(u);r.addFlashAttribute("ok","Perfil actualizado.");return "redirect:/perfil";}
}

package com.mballem.curso.security.web.controller;

import java.util.Arrays;
import java.util.List;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mballem.curso.security.domain.Medico;
import com.mballem.curso.security.domain.Perfil;
import com.mballem.curso.security.domain.PerfilTipo;
import com.mballem.curso.security.domain.Usuario;
import com.mballem.curso.security.service.MedicoService;
import com.mballem.curso.security.service.UsuarioService;

@Controller
@RequestMapping("u")
public class UsuarioController {

	@Autowired
	private UsuarioService service;

	@Autowired
	private MedicoService medicoService;

	// Abrir cadastro de usuarios (medico/admin/paciente)
	@GetMapping({ "/novo/cadastro/usuario" })
	public String cadastroPorAdminParaAdminMedicoPaciente(Usuario usuario) {
		return "usuario/cadastro";
	}

	// Abriar a lista de usuarios
	@GetMapping({ "/lista" })
	public String listarUsuarios() {
		return "usuario/lista";
	}

	// Abriar a lista de usuarios
	@GetMapping({ "/datatables/server/usuarios" })
	public ResponseEntity<?> listarUsuariosDatatables(HttpServletRequest request) {
		return ResponseEntity.ok(service.buscarTodos(request));
	}

	// savar cadastro de usuarios por administrador
	@PostMapping("/cadastro/salvar")
	public String salvarUsuarios(Usuario usuario, RedirectAttributes attr) {
		List<Perfil> perfis = usuario.getPerfis();

		if (perfis.size() > 2
				|| perfis.containsAll(
						Arrays.asList(new Perfil(PerfilTipo.ADMIN.getCod()), new Perfil(PerfilTipo.PACIENTE.getCod())))
				|| perfis.containsAll(Arrays.asList(new Perfil(PerfilTipo.MEDICO.getCod()),
						new Perfil(PerfilTipo.PACIENTE.getCod())))) {

			attr.addFlashAttribute("falha", "Peciente não pode ser Admin e/ou Médico");
			attr.addFlashAttribute("usuario", usuario);
		} else {
			try {
				service.salvarUsuario(usuario);
				attr.addFlashAttribute("sucesso", "Operação realizado com sucesso!");

			} catch (DataIntegrityViolationException e) {
				attr.addFlashAttribute("falha", "Cadastro não realizado, email já existente");
			}

		}
		return "redirect:/u/novo/cadastro/usuario";
	}

	// pre edicao de credenciais de usuarios
	@GetMapping({ "/editar/credenciais/usuario/{id}" })
	public ModelAndView preEditarCredenciais(@PathVariable("id") Long id) {
		return new ModelAndView("usuario/cadastro", "usuario", service.buscarPorId(id));
	}

	// pre editar cadastros dados pessoais
	@GetMapping("/editar/dados/usuario/{id}/perfis/{perfis}")
	public ModelAndView preEditarCadastroDadosPessoais(@PathVariable("id") Long id,
			@PathVariable("perfis") Long[] perfisId) {

		Usuario us = service.buscarPorIdEPerfis(id, perfisId);

		if (us.getPerfis().contains(new Perfil(PerfilTipo.ADMIN.getCod()))
				&& !us.getPerfis().contains(new Perfil(PerfilTipo.MEDICO.getCod()))) {
			return new ModelAndView("usuario/cadastro", "usuario", us);
		} else if (us.getPerfis().contains(new Perfil(PerfilTipo.MEDICO.getCod()))) {
			Medico medico = medicoService.buscarPorUsuarioId(id);

			return medico.hasNotId() ? new ModelAndView("medico/cadastro", "medico", new Medico(new Usuario(id)))
					: new ModelAndView("medico/cadastro", "medico", medico);

		} else if (us.getPerfis().contains(new Perfil(PerfilTipo.PACIENTE.getCod()))) {
			ModelAndView model = new ModelAndView("error");
			model.addObject("status", "403");
			model.addObject("error", "Área Restrita.");
			model.addObject("message", "Os dados de paciente são restritos a ele.");
			return model;
		}
		return new ModelAndView("redirect:/u/lista");
	}

	@GetMapping("/editar/senha")
	public String abrirEditarSenha() {
		return "usuario/editar-senha";
	}

	@PostMapping("/confirmar/senha")
	public String editarSenha(@RequestParam("senha1") String s1, @RequestParam("senha2") String s2,
			@RequestParam("senha3") String s3, @AuthenticationPrincipal User user, RedirectAttributes attr) {

		if (!s1.equals(s2)) {
			attr.addFlashAttribute("falha", "Senhas não conferem, tente novamente");
			return "redirect:/u/editar/senha";
		}

		Usuario u = service.buscarPorEmail(user.getUsername());
		if (!UsuarioService.isSenhaCorreta(s3, u.getSenha())) {
			attr.addFlashAttribute("falha", "Senha atual não conferem, tente novamente");
			return "redirect:/u/editar/senha";
		}
		service.alterarSenha(u, s1);
		attr.addFlashAttribute("sucesso", "Senha alterada com sucesso");
		return "redirect:/u/editar/senha";
	}

	/* pagina de resposta do cadastro de paciente */
	@GetMapping("/novo/cadastro")
	public String novoCadastroa(Usuario usuaio) {
		return "cadastrar-se";
	}

	/* pagina de resposta do cadastro de paciente */
	@GetMapping("/cadastro/realizado")
	public String cadastroRealizado() {
		return "fragments/mensagem";
	}

	/* recebe o form da pagina cadastrar-se */
	@PostMapping("/cadastro/paciente/salvar")
	public String salvarCadastroPaciente(Usuario usuario, BindingResult result) throws MessagingException {

		try {
			service.salvarCadastroPaciente(usuario);
		} catch (DataIntegrityViolationException ex) {
			result.reject("email", "Opss... esse email ja existe na base de dados");
			return "cadastrar-se";
		}
		return "redirect:/u/cadastro/realizado";
	}

	@GetMapping("/confirmacao/cadastro")
	public String respostaConfirmacaoCadastroPaciente(@RequestParam("codigo") String codigo, RedirectAttributes attr) {
		service.ativarCadastroPaciente(codigo);
		attr.addFlashAttribute("alerta", "sucesso");
		attr.addFlashAttribute("titulo", "Cadastro ativado!");
		attr.addFlashAttribute("text", "Parabéns seu cadastro foi ativo.");
		attr.addFlashAttribute("subtext", "Siga com seu login/senha");
		return "redirect:/login";
	}
	
	//abre a pagina de pedido de redecinicao de  senha
	@GetMapping("/p/redefinir/senha")
	public String pedidoRedefinirSenha() {
		return "usuario/pedido-recuperar-senha";
	}
	
	//form de pedido recuperar senha
	@GetMapping("/p/recuperar/senha")
	public String redefinirSenha(String email, ModelMap model) throws MessagingException {
		service.pedidoRedefinicaoDeSenha(email);
		model.addAttribute("sucesso", "Em instantes você receberá um e-mail para prosseguir com a redefinição da sua senha.");
		model.addAttribute("usuario", new Usuario(email));
		
		return "usuario/recuperar-senha";
	}
	
	//salvar a nova senha via recuperacao de senha
	@PostMapping("/p/nova/senha")
	public String confirmacaoDeRedefinicaoDeSenha(Usuario usuario, ModelMap model) {
		Usuario u = service.buscarPorEmail(usuario.getEmail());
		
		if(!usuario.getCodigoVerificador().equals(u.getCodigoVerificador())) {
			model.addAttribute("falha", "Codigo verificador não confere.");
			return "usuario/recuperar-senha";
		}
		u.setCodigoVerificador(null);
		//service.alterarSenha(usuario,  usuario.getSenha());
		
		model.addAttribute("alerta", "sucesso");
		model.addAttribute("titulo", "Senha redefinida");
		model.addAttribute("text", "Você já pode logar no sistema");
		
		return "login";
	}
	
}	

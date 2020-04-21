package com.mballem.curso.security.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mballem.curso.security.domain.Paciente;
import com.mballem.curso.security.domain.Usuario;
import com.mballem.curso.security.service.PacienteService;
import com.mballem.curso.security.service.UsuarioService;

@Controller
@RequestMapping("pacientes")
public class PacienteController {

	@Autowired
	private PacienteService service;

	@Autowired
	private UsuarioService usuarioService;

	@GetMapping("/dados")
	public String cadastrar(Paciente paciente, Model model, @AuthenticationPrincipal User user) {
		paciente = service.buscarPacientePorEmail(user.getUsername());
		if (paciente.hasNotId()) {
			paciente.setUsuario(new Usuario(user.getUsername()));
		}
		model.addAttribute("paciente", paciente);
		return "paciente/cadastro";
	}

	// Salvar paciente
	@PostMapping("/salvar")
	public String salvar(Paciente paciente, ModelMap model, RedirectAttributes attr,
			@AuthenticationPrincipal User user) {
		Usuario usuario = usuarioService.buscarPorEmail(user.getUsername());
		if (UsuarioService.isSenhaCorreta(paciente.getUsuario().getSenha(), usuario.getSenha())) {
			paciente.setUsuario(usuario);
			model.addAttribute("sucesso", "Seus dados foram incluidos com sucesso");
			service.salvar(paciente);
		} else {
			model.addAttribute("falha", "Sua senha nao confere, tente novamente");
		}
		return "paciente/cadastro";
	}

	// Editar paciente
	//
	@PostMapping("/editar")
	public String editar(Paciente paciente, ModelMap model, RedirectAttributes attr, @AuthenticationPrincipal User user) {
		Usuario usuario = usuarioService.buscarPorEmail(user.getUsername());
		if (UsuarioService.isSenhaCorreta(paciente.getUsuario().getSenha(), usuario.getSenha())) {
			model.addAttribute("sucesso", "Seus dados foram alterados com sucesso");
			service.editar(paciente);
		} else {
			model.addAttribute("falha", "Sua senha nao confere, tente novamente");
		}
		return "paciente/cadastro";
	}

}

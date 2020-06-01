package com.mballem.curso.security.web.controller;

import java.time.LocalDate;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mballem.curso.security.domain.Agendamento;
import com.mballem.curso.security.domain.Especialidade;
import com.mballem.curso.security.domain.Paciente;
import com.mballem.curso.security.domain.PerfilTipo;
import com.mballem.curso.security.service.AgendamentoService;
import com.mballem.curso.security.service.EspecialidadeService;
import com.mballem.curso.security.service.PacienteService;

@Controller
@RequestMapping("agendamentos")
public class AgendamentoController {

	@Autowired
	private AgendamentoService service;

	@Autowired
	private PacienteService pacienteService;

	@Autowired
	private EspecialidadeService especialidadeService;

	@PreAuthorize("hasAnyAuthority('PACIENTE', 'MEDICO')")
	@GetMapping({ "/agendar" })
	public String agendar(Agendamento agendamento) {
		return "agendamento/cadastro";
	}

	@PreAuthorize("hasAnyAuthority('PACIENTE', 'MEDICO')")
	@GetMapping({ "/horario/medico/{id}/data/{data}" })
	public ResponseEntity<?> getHorarios(@PathVariable("id") Long id,
			@PathVariable("data") @DateTimeFormat(iso = ISO.DATE) LocalDate data) {
		return ResponseEntity.ok(service.buscarHorariosNaoAgendadosPorMedicoIdEData(id, data));
	}

	@PreAuthorize("hasAnyAuthority('PACIENTE')")
	@PostMapping({ "/salvar" })
	public String salvar(Agendamento agendamento, RedirectAttributes attr, @AuthenticationPrincipal User user) {
		Paciente paciente = pacienteService.buscarPacientePorEmail(user.getUsername());
		String titulo = agendamento.getEspecialidade().getTitulo();
		Especialidade especialidade = especialidadeService.buscarPorTitulo(new String[] { titulo }).stream().findFirst()
				.get();
		agendamento.setEspecialidade(especialidade);
		agendamento.setPaciente(paciente);
		service.salvar(agendamento);
		attr.addFlashAttribute("sucesso", "Sua consulta foi agendada com sucesso!");

		return "redirect:/agendamentos/agendar";
	}

	@PreAuthorize("hasAnyAuthority('PACIENTE', 'MEDICO')")
	@GetMapping({ "/historico/paciente", "historico/consultas" })
	public String historico() {
		return "agendamento/historico-paciente";
	}

	@PreAuthorize("hasAnyAuthority('PACIENTE', 'MEDICO')")
	@GetMapping("/datatables/server/historico")
	public ResponseEntity<?> historicoAgendamentoPorPaciente(HttpServletRequest request,
			@AuthenticationPrincipal User user) {
		if (user.getAuthorities().contains(new SimpleGrantedAuthority(PerfilTipo.PACIENTE.getDesc()))) {
			return ResponseEntity.ok(service.buscarHistoricoPorPacienteEmail(user.getUsername(), request));
		}
		if (user.getAuthorities().contains(new SimpleGrantedAuthority(PerfilTipo.MEDICO.getDesc()))) {
			return ResponseEntity.ok(service.buscarHistoricoPorMedicoEmail(user.getUsername(), request));
		}
		return ResponseEntity.notFound().build();
	}

	/* Localizar agendamento pelo id e envia-lo para a pagina de cadastro */
	@PreAuthorize("hasAnyAuthority('PACIENTE', 'MEDICO')")
	@GetMapping("/editar/consulta/{id}")
	public String preEditarAgendamentoPorPaciente(@PathVariable("id") Long id, ModelMap model,
			@AuthenticationPrincipal User user) {
		Agendamento agendamento = service.buscaAgendamentoPorIdEUsuario(id, user.getUsername());
		model.addAttribute("agendamento", agendamento);// enviando objeto agendamento para a pagina html
		return "agendamento/cadastro";
	}

	@PreAuthorize("hasAnyAuthority('PACIENTE', 'MEDICO')")
	@PostMapping("/editar")
	public String editarConsultaAgendamento(Agendamento agendamento, RedirectAttributes attr,
			@AuthenticationPrincipal User user) {
		String titulo = agendamento.getEspecialidade().getTitulo();
		Especialidade especialidade = especialidadeService.buscarPorTitulo(new String[] { titulo }).stream().findFirst()
				.get();
		agendamento.setEspecialidade(especialidade);
		service.editarAgendamento(agendamento, user.getUsername());
		attr.addFlashAttribute("sucesso", "Sua consulta foi alterado com sucesso.");
		return "redirect:/agendamentos/agendar";

	}

	@PreAuthorize("hasAnyAuthority('PACIENTE')")
	@GetMapping("/excluir/consulta/{id}")
	public String excluirAgendamentoPorPaciente(@PathVariable("id") Long id, RedirectAttributes attr,
			@AuthenticationPrincipal User user) {
		service.excluirAgendamentoIdUsuario(id);
		attr.addFlashAttribute("sucesso", "Agendamento removido com sucesso");
		return "redirect:/agendamentos/historico/paciente";
	}

}

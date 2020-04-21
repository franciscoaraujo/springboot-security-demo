package com.mballem.curso.security.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mballem.curso.security.domain.Medico;
import com.mballem.curso.security.repository.MedicoRepository;

@Service
public class MedicoService {

	@Autowired
	private MedicoRepository repository;

	@Transactional(readOnly = true)
	public Medico buscarPorUsuarioId(Long id) {
		return repository.findByUsuarioId(id).orElse(new Medico());
	}

	@Transactional(readOnly = false)
	public void salvar(Medico medico) {
		repository.save(medico);
	}

	@Transactional(readOnly = false) /*
										 * obs: prestar atençao no readOnly false quer dizer que vai gravar no banco e
										 * nao apenas ler
										 */
	public void editar(Medico medico) {// nao precisar usar o save porque o JPA altera aumaticamente no tabela
		Medico m2 = repository.findById(medico.getId()).get();
		m2.setCrm(medico.getCrm());
		m2.setDtInscricao(medico.getDtInscricao());
		m2.setNome(medico.getNome());
		m2.setId(medico.getId());
		if (!medico.getEspecialidades().isEmpty()) {
			m2.setEspecialidades(medico.getEspecialidades());
		}
		repository.save(m2);
	}

	@Transactional(readOnly = false)
	public Medico buscarPorEmail(String email) {
		return repository.findaByUsuarioEmail(email).orElse(new Medico());
	}

	@Transactional(readOnly = false)/*variavel m2 esta no estado persistente, por isso vai atualizar isso no banco de dados de forma automatica*/
	public void excluirEspecialidadePorMedico(Long idMed, Long idEsp) {
		Medico m2 = repository.findById(idMed).get();
		m2.getEspecialidades().removeIf(filter -> filter.getId().equals(idEsp));
	}
	
	@Transactional(readOnly = true)
	public List<Medico> buscarMedicosPorEspecialidades(String titulo) {
		return repository.findByMedicosPorEspecialidades(titulo);
	}
}

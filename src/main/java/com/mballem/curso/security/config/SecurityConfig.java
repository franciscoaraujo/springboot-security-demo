package com.mballem.curso.security.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.mballem.curso.security.domain.PerfilTipo;
import com.mballem.curso.security.service.UsuarioService;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Autowired
	private UsuarioService service;
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		
		http.authorizeRequests()
			/*acessos publicod libarados*/
			.antMatchers("/webjars/**","/css/**","/image/**","/js/**").permitAll()
			.antMatchers("/","/home/").permitAll()
			
			/*acessos privados admin*/
			.antMatchers("/u/editar/senha", "/u/confirmar/senha")
				.hasAnyAuthority(PerfilTipo.MEDICO.getDesc(), PerfilTipo.PACIENTE.getDesc())
			.antMatchers("/u/**")
				.hasAnyAuthority(PerfilTipo.ADMIN.getDesc())
			
			/*acessos privados medicos*/
			.antMatchers("/medicos/especialidade/titulo/*").hasAnyAuthority(PerfilTipo.PACIENTE.getDesc())
			.antMatchers("/medicos/dados", "/medicos/salvar", "/medicos/editar")
				.hasAnyAuthority(PerfilTipo.MEDICO.getDesc(),PerfilTipo.ADMIN.getDesc())
			.antMatchers("/medicos/**")
				.hasAnyAuthority(PerfilTipo.MEDICO.getDesc())
				
				
				
			/*acessos privados paciente*/
			.antMatchers("/pacientes/dados", "/agendamento/cadastro", "/agendamentos/salvar")
				.hasAnyAuthority(PerfilTipo.PACIENTE.getDesc())
			.antMatchers("/pacientes/**")
				.hasAnyAuthority(PerfilTipo.PACIENTE.getDesc())
				
			
			/*acesso privado especialidades*/
			.antMatchers("/especialidades/datatables/server/medico/*")
				.hasAnyAuthority(PerfilTipo.MEDICO.getDesc(), PerfilTipo.ADMIN.getDesc())
			.antMatchers("/especialidades/titulo")
				.hasAnyAuthority(PerfilTipo.MEDICO.getDesc(), PerfilTipo.ADMIN.getDesc(), PerfilTipo.PACIENTE.getDesc())
			.antMatchers("/especialidades/**")
				.hasAnyAuthority(PerfilTipo.ADMIN.getDesc())
			
			.anyRequest().authenticated()
			.and()
				.formLogin()
				.loginPage("/login")
				.defaultSuccessUrl("/", true)
				.failureUrl("/login-error")
				.permitAll()
			.and()
				.logout()
				.logoutSuccessUrl("/")
			.and()
				.exceptionHandling()
				.accessDeniedPage("/acesso-negado");
	}
	
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(service).passwordEncoder(new BCryptPasswordEncoder());
	}
}

INSERT INTO usuarios (id,ativo,email,senha) VALUES (1,1,'admin@clinica.com.br','$2a$10$.F46G/xyDa./MGzFaGAocedxR07U9OILRf3JdOfse0WsFXPW5SbNS');
INSERT INTO demo_security.perfis(id, descricao)values(1, "ADMIN");
INSERT INTO demo_security.perfis(id, descricao)values(2, "MEDICO");
INSERT INTO demo_security.perfis(id, descricao)values(3, "PACIENTE");
INSERT INTO usuarios_tem_perfis (usuario_id,perfil_id) VALUES (1,1);

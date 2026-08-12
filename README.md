# ToDoList API - Java com Spring Boot

Projeto desenvolvido durante o **minicurso gratuito *Java com Spring Boot - Curso Introdutório* da Rocketseat**.

A aplicação consiste em uma **API REST para gerenciamento de tarefas (To-Do List)**, construída com Java e Spring Boot, aplicando conceitos fundamentais de desenvolvimento backend, autenticação, persistência de dados e validação de regras de negócio.

## Objetivo

Desenvolver uma API completa para gerenciamento de tarefas, permitindo:

* cadastro de usuários;
* autenticação via Basic Auth;
* criação de tarefas;
* listagem de tarefas do usuário autenticado;
* atualização completa e parcial de tarefas;
* validação de regras de negócio;
* persistência em banco de dados H2.

## Tecnologias utilizadas

* Java 21
* Spring Boot 4
* Spring Web MVC
* Spring Data JPA
* H2 Database
* Maven
* Lombok
* BCrypt
* JUnit 5
* MockMvc

## Funcionalidades

### Usuários

* criar usuário;
* validar username único;
* criptografar senha com BCrypt;
* impedir cadastro inválido.

### Tarefas

* criar tarefa autenticada;
* listar tarefas do usuário autenticado;
* atualizar tarefa;
* atualização parcial (PATCH);
* validar propriedade da tarefa;
* validar título e datas.

## Regras de negócio

* username obrigatório;
* password obrigatória;
* username único;
* título obrigatório;
* título com no máximo 50 caracteres;
* datas obrigatórias;
* data de início menor que data de término;
* tarefas pertencem ao usuário autenticado;
* usuário só pode alterar suas próprias tarefas.

## Estrutura do projeto

src/main/java

* controller
* filter
* task
* user
* utils

## Autenticação

A API utiliza **Basic Authentication**, onde o filtro autentica o usuário e associa o `idUser` à requisição.

Exemplo:

Authorization: Basic base64(username:password)

## Exemplo de criação de tarefa

POST /tasks/

```json
{
  "title": "Estudar Spring Boot",
  "description": "Implementar autenticação",
  "startAt": "2026-08-12T09:00:00",
  "endAt": "2026-08-12T11:00:00",
  "priority": "ALTA"
}
```

## Testes automatizados

Foram implementados testes utilizando **JUnit 5 e MockMvc** para validar:

* cadastro de usuários;
* autenticação;
* criação de tarefas;
* autorização;
* validações de entrada;
* regras de negócio.

A suíte possui **21 testes automatizados**, todos executando com sucesso.

Executar os testes:

```bash
mvn test
```

## Como executar

### Pré-requisitos

* Java 21
* Maven

### Clonar o projeto

```bash
git clone https://github.com/waldsonfs/rocketseat-java-springboot-todolist.git
```

### Executar

```bash
mvn spring-boot:run
```

A aplicação ficará disponível em:

http://localhost:8080

Console H2:

http://localhost:8080/h2-console

## Aprendizados

Durante o desenvolvimento deste projeto foram praticados:

* criação de APIs REST;
* arquitetura MVC;
* JPA e persistência;
* autenticação;
* filtros HTTP;
* validação de regras de negócio;
* testes automatizados;
* boas práticas de organização de projeto.

## Créditos

Projeto desenvolvido como parte do **Java com Spring Boot - Curso Introdutório**, ministrado pela **Rocketseat**.

Este repositório representa minha implementação e evolução do projeto proposto no curso, incluindo melhorias de validação, autenticação e testes automatizados.

---

Desenvolvido por **Waldson Fernandes**

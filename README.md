<p align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="docs/assets/logo-dark.svg">
    <img src="docs/assets/logo-light.svg" alt="Bluff" width="200">
  </picture>
</p>

<h1 align="center">Bluff API</h1>

<p align="center">
  <strong>API do Bluff, um Planning Poker em que cada pessoa vota em segredo<br>
  e todos os votos são revelados juntos.</strong>
</p>

<p align="center">
  <img alt="Java" src="https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white">
  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring_Boot-4.1-6DB33F?logo=springboot&logoColor=white">
  <img alt="PostgreSQL" src="https://img.shields.io/badge/PostgreSQL-17-4169E1?logo=postgresql&logoColor=white">
  <img alt="Flyway" src="https://img.shields.io/badge/Flyway-migrations-CC0200?logo=flyway&logoColor=white">
  <img alt="WebSocket" src="https://img.shields.io/badge/WebSocket-STOMP-010101">
  <img alt="Testcontainers" src="https://img.shields.io/badge/Testcontainers-integração-2496ED?logo=docker&logoColor=white">
</p>

<p align="center">
  <a href="#destaques">Destaques</a> ·
  <a href="#como-rodar">Como rodar</a> ·
  <a href="#api-rest">API REST</a> ·
  <a href="#websocket-stomp">WebSocket</a> ·
  <a href="#arquitetura">Arquitetura</a> ·
  <a href="#documentação">Documentação</a>
</p>

---

Este repositório é o **back-end** do Bluff. O front fica em
[bluff-frontend](https://github.com/NicolasWutkeBartels/bluff-frontend).

## Destaques

- **Votos ocultos garantidos no servidor:** enquanto a rodada está em votação, nenhuma resposta,
  REST ou WebSocket, traz o valor de um voto. Uma única classe (`RodadaMapper`) decide o que sai.
- **Concorrência tratada:** travas pessimistas impedem duas rodadas simultâneas e votos depois da
  revelação; constraints do banco garantem código de sala, apelido e voto únicos.
- **Tempo real:** eventos publicados via STOMP só depois do commit, com presença (online/offline)
  por participante.
- **Erros padronizados:** toda falha segue o RFC 9457 (`application/problem+json`), com um `codigo`
  estável e mensagens em português.
- **Testes de integração** com Postgres real via Testcontainers, incluindo um cliente STOMP.

## Stack

| Área | Ferramentas |
|---|---|
| Linguagem e framework | Java 21, Spring Boot 4 (Web MVC, Data JPA, Validation, WebSocket) |
| Banco | PostgreSQL 17, Flyway |
| Testes | JUnit 5, AssertJ, Testcontainers |
| Build | Maven (wrapper incluso) |

## Como rodar

**Pré-requisitos:** Java 21 e Docker.

```bash
docker compose up -d
./mvnw spring-boot:run
```

A API sobe em [http://localhost:8080](http://localhost:8080), e o Postgres de desenvolvimento
na porta `5433`. Na primeira subida, o Flyway cria as tabelas.

```bash
./mvnw test
```

Os testes sobem um Postgres próprio com Testcontainers (o Docker precisa estar rodando).

### Variáveis de ambiente

| Variável | Padrão | Descrição |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5433/bluff` | Conexão com o banco |
| `DB_USUARIO` | `bluff` | Usuário do banco |
| `DB_SENHA` | `bluff` | Senha do banco |
| `CORS_ORIGENS` | `http://localhost:3000` | Origens do front (REST e WebSocket), separadas por vírgula |
| `SERVER_PORT` | `8080` | Porta da API |

## API REST

Não há login: ao criar ou entrar numa sala, a pessoa recebe um `participante.id`. As ações de
rodada exigem esse id no cabeçalho `X-Participante-Id`.

| Método | Caminho | Descrição |
|---|---|---|
| `GET` | `/dominios?dominio=RODADA` | Valores de um domínio da tabela `status` |
| `GET` | `/baralhos` | Baralhos disponíveis e suas cartas |
| `GET` | `/baralhos/{codigo}/cartas` | Cartas de um baralho |
| `POST` | `/salas` | Cria a sala e o primeiro participante |
| `POST` | `/salas/{codigo}/participantes` | Entra na sala |
| `GET` | `/salas/{codigo}` | Estado atual: participantes, presença e rodada atual |
| `POST` | `/salas/{codigo}/rodadas` | Começa uma rodada |
| `GET` | `/salas/{codigo}/rodadas?status=REVEALED` | Histórico, com filtro opcional pelo código do status |
| `POST` | `/rodadas/{id}/votos` | Vota ou troca o voto (responde `204`, sem corpo) |
| `POST` | `/rodadas/{id}/revelar` | Revela os votos, com `estimativaFinal` opcional |

### Erros

```json
{
  "status": 409,
  "title": "Conflict",
  "detail": "O apelido Ana já está em uso nesta sala. Escolha outro.",
  "codigo": "APELIDO_EM_USO",
  "campos": { "apelido": "..." }
}
```

`campos` só aparece em erros de validação. O front mostra o `detail` e usa o `codigo` quando
precisa reagir a um erro específico.

## WebSocket (STOMP)

Conecte em `/ws`, enviando `codigoSala` e `participanteId` no frame `CONNECT` para contar presença,
e assine `/topic/salas/{codigo}`. As mensagens têm o formato `{ "tipo": "...", "dados": { ... } }`.

| Tipo | Dados |
|---|---|
| `PARTICIPANTE_ENTROU` | participante (também quando alguém volta a ficar online) |
| `PARTICIPANTE_SAIU` | `participanteId` |
| `RODADA_INICIADA` | rodada |
| `VOTO_REGISTRADO` | `rodadaId` e `participanteId`, **nunca o valor** |
| `RODADA_REVELADA` | rodada com todos os valores |

Eventos perdidos durante uma queda não são reenviados: ao reconectar, o cliente busca
`GET /salas/{codigo}` de novo.

## Arquitetura

Pacotes organizados por camada, com dependências sempre descendo
(`controller → service → repository/model`):

```
dev.bluff
├── config/        CORS e WebSocket
├── controller/    traduz HTTP para chamadas de service
├── service/       regras de negócio e transações
├── repository/    Spring Data JPA
├── model/         entidades (+ enums/)
├── dto/           requests e responses, separados por endpoint (sala/, rodada/, voto/...)
├── mapper/        entidade → DTO (aplica a regra dos votos ocultos)
├── exception/     erros de negócio e tratamento global
├── websocket/     eventos, publicação STOMP e presença
└── util/          gerador do código da sala
```

## Documentação

- [Aprendizado](docs/aprendizado.md): o caminho de um voto do clique até o banco, as camadas e o STOMP.
- [Modelagem](docs/modelagem.md): tabelas e onde cada regra é garantida.
- [Convenções](docs/convencoes.md): pacotes e nomes.
- [Prefixos de coluna](docs/prefixos.md): convenção de nomes do banco.

## Limites conhecidos

- Presença e broker STOMP ficam em memória: a API foi pensada para rodar em uma instância.
- Sem login: quem tiver o `participanteId` age como aquela pessoa. A tabela `usuario` já existe
  para um login futuro.

# Bluff (API)

API do Bluff, um Planning Poker: cada pessoa vota em segredo e todos os votos são revelados juntos.
Java 21, Spring Boot 4, PostgreSQL, Flyway e WebSocket (STOMP).

O front fica em outro repositório (`bluff-frontend`).

## Rodar localmente

Pré-requisitos: Java 21 e Docker.

```bash
docker compose up -d     # Postgres na porta 5433
./mvnw spring-boot:run   # API em http://localhost:8080
./mvnw test              # testes de integração (Testcontainers sobe um Postgres próprio)
```

Na primeira subida, o Flyway cria as tabelas.

## Variáveis de ambiente

| Variável       | Padrão                                   | Para quê                                  |
|----------------|------------------------------------------|-------------------------------------------|
| `DB_URL`       | `jdbc:postgresql://localhost:5433/bluff` | conexão com o banco                       |
| `DB_USUARIO`   | `bluff`                                  |                                           |
| `DB_SENHA`     | `bluff`                                  |                                           |
| `CORS_ORIGENS` | `http://localhost:3000`                  | origens do front (REST e WebSocket), separadas por vírgula |

## API REST

Ações de rodada exigem o cabeçalho `X-Participante-Id` (o id recebido ao criar ou entrar na sala).

| Método e caminho | O que faz |
|---|---|
| `GET /dominios?dominio=RODADA` | Valores de um domínio da tabela `status` (lista vazia se não existir) |
| `GET /baralhos` | Baralhos e suas cartas |
| `GET /baralhos/{codigo}/cartas` | Cartas de um baralho |
| `POST /salas` | Cria a sala e o primeiro participante |
| `POST /salas/{codigo}/participantes` | Entra na sala |
| `GET /salas/{codigo}` | Estado atual: participantes (com `online`), rodada atual, quem já votou |
| `POST /salas/{codigo}/rodadas` | Começa uma rodada |
| `GET /salas/{codigo}/rodadas?status=REVEALED` | Histórico (filtro opcional pelo código do status) |
| `POST /rodadas/{id}/votos` | Vota ou troca o voto. Responde 204, sem corpo |
| `POST /rodadas/{id}/revelar` | Revela os votos; aceita `estimativaFinal` opcional |

**Votos ocultos:** enquanto a rodada está em `VOTING`, nenhuma resposta (REST ou WebSocket)
traz o valor de um voto, só quem votou (`valor: null`).

Erros seguem o RFC 9457 (`application/problem+json`), com um campo extra `codigo`
(ex.: `APELIDO_EM_USO`) e, em erros de validação, `campos`.

## WebSocket (STOMP)

Conecte em `/ws` (mandando `codigoSala` e `participanteId` no CONNECT para contar presença)
e assine `/topic/salas/{codigo}`. Mensagens: `{"tipo": "...", "dados": {...}}`.

| Tipo | Dados |
|---|---|
| `PARTICIPANTE_ENTROU` | participante (também quando alguém volta a ficar online) |
| `PARTICIPANTE_SAIU` | `participanteId` |
| `RODADA_INICIADA` | rodada |
| `VOTO_REGISTRADO` | `rodadaId`, `participanteId` (nunca o valor) |
| `RODADA_REVELADA` | rodada com todos os valores |

Ao reconectar, o cliente deve buscar `GET /salas/{codigo}` de novo: eventos perdidos não são reenviados.

## Documentação

- [docs/aprendizado.md](docs/aprendizado.md): o caminho de um voto, as camadas, Flyway × JPA, STOMP e o que ler primeiro
- [docs/modelagem.md](docs/modelagem.md): tabelas e onde cada regra é garantida
- [docs/prefixos.md](docs/prefixos.md): convenção de nomes de coluna

## Limites conhecidos

- Presença e broker STOMP ficam em memória: a API foi feita para rodar em uma instância.
- Sem login: quem tiver o `participanteId` age como aquela pessoa. A tabela `usuario` já existe para o login futuro.

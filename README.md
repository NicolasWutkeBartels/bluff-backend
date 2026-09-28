# Bluff (API)

API do Bluff, um Planning Poker. Java 21, Spring Boot, PostgreSQL e Flyway.

O front fica em outro repositório (`bluff-frontend`).

## Rodar localmente

Pré-requisitos: Java 21 e Docker.

```bash
docker compose up -d     # Postgres na porta 5433
./mvnw spring-boot:run   # API na porta 8080
```

Na primeira subida, o Flyway cria as tabelas.

## Variáveis de ambiente

| Variável       | Padrão                                     | Para quê                         |
|----------------|--------------------------------------------|----------------------------------|
| `DB_URL`       | `jdbc:postgresql://localhost:5433/bluff`   | conexão com o banco              |
| `DB_USUARIO`   | `bluff`                                    |                                  |
| `DB_SENHA`     | `bluff`                                    |                                  |
| `CORS_ORIGENS` | `http://localhost:3000`                    | origens do front, separadas por vírgula |

## Documentação

- [docs/modelagem.md](docs/modelagem.md): tabelas e onde cada regra é garantida
- [docs/prefixos.md](docs/prefixos.md): convenção de nomes de coluna

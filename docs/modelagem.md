# Modelagem

## Diagrama

```
               usuario ─────────0..1───────┐
                                           ▼
   sala ────────1:N────────────────► participante
    │                                      │
    │ 1:N                                  │ 1:N
    ▼                                      ▼
  rodada ───────────1:N────────────────► voto
    ▲
    │ N:1
  status
```

Lendo as setas: uma sala tem vários participantes e várias rodadas; cada rodada tem
vários votos, e cada voto pertence a um participante. A rodada aponta para um status.
O participante pode (no futuro) apontar para um usuário.

| FK                      | Aponta para      | Nula? |
|-------------------------|------------------|-------|
| `participante.sal_id`   | `sala.sal_id`    | não   |
| `participante.usu_id`   | `usuario.usu_id` | sim   |
| `rodada.sal_id`         | `sala.sal_id`    | não   |
| `rodada.sta_id`         | `status.sta_id`  | não   |
| `voto.rod_id`           | `rodada.rod_id`  | não   |
| `voto.par_id`           | `participante.par_id` | não |

## Tabelas

### usuario
Pessoa com login. **Ainda não usada**: foi criada agora para que o login futuro
só precise preencher `participante.usu_id`, sem alterar tabelas existentes.

| Coluna          | Tipo         | Regra                  |
|-----------------|--------------|------------------------|
| `usu_id`        | UUID         | PK                     |
| `usu_nome`      | varchar(100) | obrigatório            |
| `usu_email`     | varchar(255) | obrigatório, único     |
| `usu_senha`     | varchar(100) | hash BCrypt            |
| `usu_criado_em` | timestamptz  | padrão `now()`         |

### status
Valores fixos do sistema, agrupados por domínio. É a tabela que alimenta os selects do front
(`GET /dominios?dominio=RODADA`).

| Coluna          | Tipo         | Regra                                   |
|-----------------|--------------|-----------------------------------------|
| `sta_id`        | UUID         | PK                                      |
| `sta_dominio`   | varchar(50)  | obrigatório                             |
| `sta_codigo`    | varchar(50)  | obrigatório; único com `sta_dominio`    |
| `sta_descricao` | varchar(100) | texto exibido ao usuário                |

Valores iniciais:

| Domínio  | Código     | Descrição    |
|----------|------------|--------------|
| `RODADA` | `VOTING`   | Em votação   |
| `RODADA` | `REVEALED` | Revelada     |

**Por que buscar sempre por código?** O UUID é gerado em cada banco (dev, teste, produção),
então muda entre ambientes. O código é escrito na migration e é igual em todos.
Por isso o front envia `sta_codigo` e o back usa um único método,
`StatusService.buscarPorCodigo(dominio, codigo)`, para transformar o código na entidade.

### sala

| Coluna          | Tipo        | Regra                                            |
|-----------------|-------------|--------------------------------------------------|
| `sal_id`        | UUID        | PK                                               |
| `sal_codigo`    | varchar(6)  | único; 6 caracteres sem ambíguos (sem 0 O 1 I L) |
| `sal_nome`      | varchar(80) | nome próprio da sala                             |
| `sal_baralho`   | varchar(20) | padrão `FIBONACCI`; validado pelo enum `Baralho` |
| `sal_criado_em` | timestamptz |                                                  |

### participante
Quem está numa sala. Sem login, o `par_id` funciona como "chave de acesso":
é um UUID difícil de adivinhar, que o front guarda no `localStorage`.

| Coluna          | Tipo        | Regra                                            |
|-----------------|-------------|--------------------------------------------------|
| `par_id`        | UUID        | PK                                               |
| `sal_id`        | UUID        | FK → sala                                        |
| `usu_id`        | UUID        | FK → usuario, **nula**                           |
| `par_apelido`   | varchar(30) | único dentro da sala (`sal_id`, `par_apelido`)   |
| `par_papel`     | varchar(20) | padrão `VOTANTE`; validado pelo enum `Papel`     |
| `par_criado_em` | timestamptz |                                                  |

### rodada
Uma tarefa sendo estimada.

| Coluna                 | Tipo         | Regra                                  |
|------------------------|--------------|----------------------------------------|
| `rod_id`               | UUID         | PK                                     |
| `sal_id`               | UUID         | FK → sala                              |
| `sta_id`               | UUID         | FK → status (domínio `RODADA`)         |
| `rod_titulo`           | varchar(200) | obrigatório                            |
| `rod_estimativa_final` | varchar(10)  | nula; definida ao revelar              |
| `rod_criado_em`        | timestamptz  |                                        |

### voto

| Coluna          | Tipo        | Regra                                        |
|-----------------|-------------|----------------------------------------------|
| `vot_id`        | UUID        | PK                                           |
| `rod_id`        | UUID        | FK → rodada                                  |
| `par_id`        | UUID        | FK → participante                            |
| `vot_valor`     | varchar(10) | carta escolhida (texto: "8", "M")            |
| `vot_criado_em` | timestamptz |                                              |

Único em (`rod_id`, `par_id`): um voto por participante por rodada.

## Onde cada regra é garantida

| Regra                                            | Banco | Java |
|--------------------------------------------------|:-----:|:----:|
| Código de sala único                             | ✔     | gera outro e tenta de novo (até 5×) |
| Apelido único por sala                           | ✔     | converte a violação em erro 409     |
| Um voto por participante por rodada              | ✔     | atualiza o voto existente           |
| Papel e baralho válidos                          |       | ✔ (enums)                           |
| Observador não vota                              |       | ✔                                   |
| Só vota/revela rodada `VOTING`                   |       | ✔ (com lock na rodada)              |
| No máximo uma rodada `VOTING` por sala           |       | ✔ (com lock na sala)                |
| Valores de voto ocultos até a revelação          |       | ✔ (na montagem das respostas)       |

As regras "só uma rodada VOTING" e "só vota em VOTING" não viraram constraint porque
dependem do `sta_id`, cujo UUID muda por ambiente. Um índice parcial como
`WHERE sta_id = '...'` ficaria preso a um banco específico.

## Por que `par_papel` e `sal_baralho` são texto, e não linhas em `status`?

São valores que o código precisa **interpretar** (as cartas de cada baralho, a regra de que
observador não vota). Um enum no Java deixa o compilador ajudar. Já o status da rodada
é um valor que o front lista num select e que pode ganhar descrições novas sem código novo.

## Crescimento previsto

O modelo aceita tabelas novas sem retrabalho:
- **Login:** preencher `participante.usu_id`. Nenhuma coluna muda.
- **Issue board:** tabela `estoria` (`est`) e uma FK `est_id` nula em `rodada`.
  Novos status de estória entram em `status` com `sta_dominio = 'ESTORIA'`.

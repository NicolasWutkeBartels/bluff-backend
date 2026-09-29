# Convenções do código (API)

Resumo das regras de organização e de nomes. Seguem o padrão de mercado do Spring Boot
(pacotes por camada, sufixos de estereótipo) e as convenções oficiais do Java
(classes em `PascalCase`, métodos e variáveis em `camelCase`, constantes em `UPPER_SNAKE_CASE`,
pacotes em minúsculas).

## Idioma

- **Domínio em português**: `Sala`, `Rodada`, `Voto`, `criar`, `revelar`, `apelido`.
- **Estereótipo técnico em inglês**, como sufixo: `Controller`, `Service`, `Repository`,
  `Request`, `Response`, `Mapper`, `Exception`, `Handler`, `Listener`, `Publisher`,
  `Registry`, `Generator`, `Config`. É o que qualquer pessoa que conhece Spring procura.
- Mensagens para o usuário: português.

## Comentários

Sem comentários no código: nomes claros no lugar de explicação. O "porquê" das decisões
(travas, retentativas, votos ocultos) está em [aprendizado.md](aprendizado.md) e [modelagem.md](modelagem.md).

## Pacotes (`dev.bluff`)

```
config/        @Configuration: CORS, WebSocket
controller/    @RestController: só traduz HTTP → service (+ ApiHeaders)
service/       @Service: regras de negócio e transações
repository/    interfaces Spring Data
model/         entidades JPA (+ StatusRodada, os códigos de status conhecidos)
  enums/       enums do domínio (Baralho, Papel)
dto/           records que entram e saem da API, separados por endpoint
  sala/        CriarSalaRequest, EntrarSalaRequest, EntradaSalaResponse, EstadoSalaResponse
  rodada/      IniciarRodadaRequest, RevelarRodadaRequest, RodadaResponse
  voto/        VotarRequest, VotoResponse
  participante/, baralho/, dominio/
  evento/      o que trafega no WebSocket (TipoEvento, EventoMensagem, *Evento)
mapper/        entidade → DTO quando há regra na conversão (RodadaMapper: votos ocultos)
exception/     NegocioException, TipoErro, Constraints, ApiExceptionHandler
websocket/     evento interno, publicação STOMP, presença e conexões
util/          peças sem estado de negócio (CodigoSalaGenerator)
```

Dependências só descem: `controller → service → repository/model`. O controller nunca
fala com repository; entidade nunca sai pela API (sempre um `*Response`).

## Nomes

| O quê | Padrão | Exemplo |
|---|---|---|
| Controller | `<Recurso>Controller` | `SalaController` |
| Service | `<Recurso>Service` | `RodadaService` |
| Repository | `<Entidade>Repository` | `VotoRepository` |
| DTO de entrada | `<Ação><Recurso>Request` | `CriarSalaRequest`, `VotarRequest` |
| DTO de saída | `<Recurso>Response` / `<Qualificador><Recurso>Response` | `RodadaResponse`, `EstadoSalaResponse` |
| Payload de evento WS | `<Fato>Evento` | `VotoRegistradoEvento` |
| Exceção | `<Motivo>Exception` | `NegocioException` |
| Constantes | `UPPER_SNAKE_CASE` | `MAXIMO_TENTATIVAS_CODIGO` |

**Métodos são verbos** em `camelCase`:

- `buscar...` devolve um item (ou lança erro se não existe): `buscarSala`, `buscarEstado`, `buscarPorCodigo`.
- `listar...` devolve coleção: `listarHistorico`, `listarCartas`, `listarPorDominio`.
- Ações do domínio pelo nome do domínio: `criar`, `entrar`, `iniciar`, `votar`, `revelar`.
- Fábricas estáticas de DTO: `de(...)` (ex.: `ParticipanteResponse.de(participante, online)`).
- Métodos de repository seguem a convenção do Spring Data (`findBy...`, `existsBy...`);
  consultas escritas à mão ganham nome em português (`buscarComTrava`).

## Testes

- Mesmo pacote da classe testada para testes de unidade (`util/CodigoSalaGeneratorTest`).
- Testes de integração herdam de `IntegracaoTestBase` e são nomeados pelo assunto (`VotoTest`).
- Nome do método descreve o comportamento em português: `observadorNaoVota`.

## Banco

Colunas com prefixo da tabela e nomes de constraint: ver [prefixos.md](prefixos.md).

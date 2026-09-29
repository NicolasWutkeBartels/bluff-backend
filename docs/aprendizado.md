# Aprendendo com o Bluff

Guia para quem já usou Spring há muito tempo e não lembra de quase nada.
Os caminhos citados começam com o nome do repositório: `bluff-backend/` (esta API)
ou `bluff-frontend/` (o app Next.js).

---

## 1. O mapa em uma frase

O front (Next.js) chama a API por **REST** para fazer coisas (criar sala, votar, revelar) e escuta
a API por **WebSocket/STOMP** para saber o que os outros fizeram. A API guarda tudo no **PostgreSQL**,
cujas tabelas são criadas pelo **Flyway** e lidas pelo **JPA/Hibernate**.

```
 navegador                         API Spring                          banco
┌──────────┐   POST /rodadas/..   ┌────────────┐   ┌─────────┐      ┌──────────┐
│  Next.js │ ───────────────────► │ Controller │ ─►│ Service │ ───► │ Postgres │
│          │                      └────────────┘   └────┬────┘      └──────────┘
│          │   /topic/salas/X      ┌────────────┐        │ evento (após o commit)
│          │ ◄──────────────────── │  STOMP     │ ◄──────┘
└──────────┘                       └────────────┘
```

---

## 2. As camadas e o papel de cada uma

O código é organizado **por camada**: cada pacote de `dev.bluff` guarda um tipo de peça
(`controller/`, `service/`, `repository/`, `model/`, `dto/`...). Os DTOs ficam separados por
endpoint (`dto/sala/`, `dto/rodada/`...). As regras de nomes estão em [convencoes.md](convencoes.md).

| Camada | Exemplo | O que faz | O que **não** faz |
|---|---|---|---|
| **Controller** | `RodadaController` | Traduz HTTP: lê caminho, corpo, cabeçalhos; escolhe o status (201, 204). | Não decide regra de negócio. |
| **DTO** (records) | `VotarRequest`, `RodadaResponse` | Formato do que entra e sai. Validação de formato com `@NotBlank`, `@Size`. | Não é gravado no banco. |
| **Service** | `RodadaService` | As regras: quem pode votar, em que estado, com que carta. Abre a transação (`@Transactional`). | Não sabe nada de HTTP nem de STOMP. |
| **Repository** | `VotoRepository` | Conversa com o banco. O Spring Data gera a implementação a partir do nome do método (`findByRodadaAndParticipante`). | Não tem regra. |
| **Entidade** | `Voto` | Espelho de uma linha da tabela. Tem pequenos comportamentos (`trocarValor`). | Não conhece outras camadas. |

**Por que as regras ficam no service?** Porque é o único lugar por onde toda ação passa,
seja qual for a porta de entrada (hoje REST, amanhã talvez uma fila ou um comando). Se a regra
"observador não vota" ficasse no controller, uma segunda porta teria que lembrar de repeti-la.
As exceções são regras que o **banco** garante melhor que o Java (unicidade, ver seção 6).

---

## 3. O caminho de um voto, do clique até o banco (e de volta)

A Bia clica na carta **8**.

**No front**

1. `bluff-frontend/src/components/sala/mao.tsx`: o botão da carta chama `aoVotar("8")`.
2. `bluff-frontend/src/hooks/use-meu-voto.ts`: a mutation do React Query roda.
   Em `onMutate`, a carta vai para a mesa **antes** da resposta (atualização otimista) e o valor é
   guardado no `localStorage`, porque a API nunca vai devolvê-lo antes da revelação.
3. `bluff-frontend/src/services/rodada.service.ts` → `rodadaService.votar()`: o Axios faz
   `POST /rodadas/{id}/votos` com `{"valor": "8"}` e o cabeçalho `X-Participante-Id`.

**Na API**

4. `RodadaController.votar`: o Spring converte o JSON em `VotarRequest`, roda a
   validação (`@Valid`) e lê o cabeçalho como `UUID`. Chama o service.
5. `RodadaService.votar` (anotado com `@Transactional`: tudo aqui é uma transação só):
   - `rodadaRepository.buscarComTrava(id)` → `SELECT ... FOR UPDATE`. Se alguém estiver
     revelando essa rodada agora, esperamos. Assim um voto nunca entra depois da revelação.
   - confere se a Bia é da sala, se é votante, se a rodada está em `VOTING` e se "8" está no baralho;
   - procura um voto existente: se houver, `voto.trocarValor("8")`; se não, `save(new Voto(...))`.
     Repare que trocar o valor **não chama save**: a entidade está "gerenciada" pelo Hibernate, e
     no fim da transação ele percebe a mudança e faz o `UPDATE` sozinho (*dirty checking*).
   - publica `EventoSala(VOTO_REGISTRADO, {rodadaId, participanteId})`, **sem o valor**.
6. O método termina, o Spring faz o **commit**. Só então...
7. `EventoSalaPublisher.publicar` (`@TransactionalEventListener(AFTER_COMMIT)`) envia o evento para
   `/topic/salas/{codigo}`. O controller responde **204 No Content**.

**De volta aos navegadores**

8. `bluff-frontend/src/hooks/use-sala-ao-vivo.ts`: todos que assinam o tópico recebem o evento, validam com
   Zod e aplicam no cache: a carta da Bia aparece virada para baixo na mesa de todo mundo.

---

## 4. Flyway e JPA: quem manda no banco

Os dois mexem com tabelas, mas com papéis bem separados:

- **Flyway é o dono do schema.** Na subida da aplicação, ele olha `src/main/resources/db/migration`,
  compara com a tabela `flyway_schema_history` e roda, em ordem, os arquivos `V1__...sql`, `V2__...sql`
  que ainda não rodaram. Uma migration aplicada **nunca** é editada: para mudar algo, crie a próxima.
- **JPA/Hibernate só lê e escreve dados.** Com `spring.jpa.hibernate.ddl-auto=validate`, ele não cria
  nada; apenas confere, na subida, se cada `@Entity` bate com as tabelas. Se você renomear uma
  coluna na entidade e esquecer a migration, a aplicação nem sobe (é bom: o erro aparece cedo).

A ligação entre os dois é o mapeamento explícito:

```java
@Column(name = "par_apelido")   // nome da coluna, definido pela migration
private String apelido;         // nome do campo, como o Java gosta
```

As tabelas seguem a convenção de prefixos (`docs/prefixos.md`); o Java segue a dele. O `@Column`
é a ponte.

---

## 5. Como o STOMP funciona aqui

WebSocket é só um "cano" bidirecional. **STOMP** é um protocolo de mensagens por cima dele,
com a ideia de **tópicos**: quem assina um tópico recebe tudo o que for publicado nele.

- Configuração: `bluff-backend/src/main/java/dev/bluff/config/WebSocketConfig.java`.
  - `/ws` é onde o cliente abre a conexão.
  - `enableSimpleBroker("/topic")`: um broker em memória, dentro da própria API, distribui as
    mensagens. Para várias instâncias, trocaríamos por um broker externo (RabbitMQ, por exemplo).
  - Heartbeats de 10 s: cliente e servidor trocam "sinais de vida" e percebem conexões mortas.
- **Os clientes só escutam.** Toda ação vai por REST. Isso mantém validação, erros e testes num
  lugar só; o WebSocket serve apenas para avisar.
- **Presença:** no frame `CONNECT`, o front manda `codigoSala` e `participanteId`.
  `ConexaoListener` registra a sessão em `PresencaRegistry`; quando a última conexão de alguém fecha,
  sai o evento `PARTICIPANTE_SAIU`.
- **Reconexão:** o broker não guarda mensagens para quem estava fora. Então, a cada conexão
  (a primeira e as seguintes), o front busca `GET /salas/{codigo}` de novo e substitui o que tinha.
  Veja o comentário no topo de `bluff-frontend/src/hooks/use-sala-ao-vivo.ts`.
- **Por que só depois do commit?** Se o evento saísse antes, um cliente poderia reagir buscando o
  estado da sala e não encontrar o dado ainda (ou encontrar algo que depois sofreu rollback).

---

## 6. Onde ficam as regras de negócio, e por quê

| Regra | Onde | Por quê |
|---|---|---|
| Código de sala único, apelido único por sala, um voto por participante por rodada | **Banco** (constraints `uk_...`) | Só o banco garante unicidade com várias requisições ao mesmo tempo. O Java apenas traduz a violação num erro legível (`Constraints.violou`). |
| Tentar outro código de sala em caso de colisão (até 5×) | `SalaService.criar` | Cada tentativa numa transação nova (`TransactionTemplate`): depois de um erro de constraint, a transação anterior fica inutilizável. |
| Uma rodada `VOTING` por sala; só vota/revela em `VOTING` | `RodadaService` com travas pessimistas | Dependem do `sta_id`, que muda por ambiente; um índice parcial ficaria preso a um banco. A trava (`FOR UPDATE`) faz as requisições concorrentes passarem uma de cada vez. |
| Observador não vota; carta precisa existir no baralho | `RodadaService` | Regras que dependem de enums do Java (`Papel`, `Baralho`). |
| **Votos ocultos em `VOTING`** | `RodadaMapper` | Toda resposta de rodada (estado, histórico, eventos) é montada ali. Um só lugar decide se o valor sai do servidor. |
| Status sempre por domínio + código | `StatusService.buscarPorCodigo` | O UUID do status muda entre ambientes; o código, não. |
| Formato dos dados (campo vazio, tamanho) | DTOs com Bean Validation, e Zod no front | Erro rápido e específico por campo, antes de chegar à regra. |

---

## 7. Dez pontos do código para ler primeiro

1. `bluff-backend/src/main/java/dev/bluff/mapper/RodadaMapper.java`: a regra dos votos ocultos inteira em 20 linhas.
2. `bluff-backend/src/main/java/dev/bluff/service/RodadaService.java`: todas as regras de votação, com as travas.
3. `bluff-backend/src/main/java/dev/bluff/service/SalaService.java` → `criar`: retentativa do código com `TransactionTemplate`.
4. `bluff-backend/src/main/java/dev/bluff/util/CodigoSalaGenerator.java`: `SecureRandom` e o alfabeto sem ambíguos.
5. `bluff-backend/src/main/java/dev/bluff/websocket/EventoSalaPublisher.java`: do evento do Spring ao tópico STOMP, após o commit.
6. `bluff-backend/src/main/java/dev/bluff/exception/ApiExceptionHandler.java`: como toda falha vira o mesmo formato de erro.
7. `bluff-backend/src/main/resources/db/migration/`: as seis migrations, na ordem. Compare com as entidades.
8. `bluff-backend/src/test/java/dev/bluff/VotosOcultosTest.java`: a regra central provada por REST e por um cliente STOMP de verdade.
9. `bluff-frontend/src/hooks/use-sala-ao-vivo.ts`: conexão, eventos aplicados no cache e ressincronização.
10. `bluff-frontend/src/components/sala/mesa.tsx` + `components/carta/carta-que-vira.tsx`: o estado do voto como estado da carta, e o flip da revelação.

---

## 8. Comandos úteis

```bash
# API
docker compose up -d              # Postgres na porta 5433
./mvnw spring-boot:run            # API em http://localhost:8080
./mvnw test                       # testes (sobe um Postgres próprio via Testcontainers)

# Espiar o banco
docker exec -it bluff-postgres psql -U bluff -d bluff
  select * from flyway_schema_history;
  select sta_dominio, sta_codigo, sta_descricao from status;

# Front (no repositório bluff-frontend)
npm run dev                       # http://localhost:3000
```

# Prefixos de coluna

Toda coluna começa com o prefixo da tabela. Assim, numa consulta com vários `JOIN`,
`sal_nome` e `par_apelido` dizem de onde vêm sem precisar de alias.

## Regra

1. **Prefixo:** as 3 primeiras letras do nome da tabela.
   Se o nome for composto, soma-se a inicial de cada palavra seguinte.
   - `sala` → `sal`
   - `usuario_fornecedor` → `usu` + `f` → `usuf`
2. **Colisão:** se o prefixo já existir, a tabela **mais nova** acrescenta a próxima
   letra da última palavra. O prefixo antigo nunca é renomeado.
   - `usuario` já usa `usu`. Uma tabela nova `usuario_fornecedor` gera `usuf`;
     se `usuf` já existisse, ela passaria para `usufo`.
   - Uma tabela nova `salario` geraria `sal`, que já é de `sala`,
     então ficaria `sala` (próxima letra de "salario").
3. **Chave estrangeira:** mantém o nome da PK de origem.
   `participante.sal_id` aponta para `sala.sal_id`.
4. **PK:** sempre `<prefixo>_id`, do tipo `UUID` com `DEFAULT gen_random_uuid()`.
5. **`nome`** só para nomes próprios (pessoa, sala). Qualquer outro texto descritivo é `descricao`.

## Prefixos em uso

| Tabela         | Prefixo | Criada em |
|----------------|---------|-----------|
| `usuario`      | `usu`   | V1        |
| `status`       | `sta`   | V2        |
| `sala`         | `sal`   | V3        |
| `participante` | `par`   | V4        |
| `rodada`       | `rod`   | V5        |
| `voto`         | `vot`   | V6        |

Ao criar uma tabela, adicione uma linha aqui **na mesma migration/commit**.
É esta tabela que se consulta para detectar colisões.

## Exemplo: o futuro issue board

Se o Bluff ganhar um quadro de estórias, as tabelas prováveis seriam:

| Tabela nova      | Cálculo                          | Prefixo |
|------------------|----------------------------------|---------|
| `estoria`        | `est`                            | `est`   |
| `estoria_tag`    | `est` + `t` → `estt`             | `estt`  |
| `status_estoria` | `sta` + `e` → `stae`             | `stae`  |

Nenhuma colide com as atuais. A rodada ganharia uma FK `est_id` (nula) para apontar
para a estória que está sendo estimada, sem alterar as colunas existentes.

## Nomes de constraints

Não fazem parte da convenção obrigatória, mas seguem um padrão para as mensagens de erro
do banco serem legíveis (e para o código poder identificá-las, como em `uk_sala_codigo`):

- `pk_<tabela>`
- `fk_<tabela>_<tabela_referenciada>`
- `uk_<tabela>_<colunas>`
- `ix_<tabela>_<colunas>`

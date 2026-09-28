package dev.bluff.erro;

import org.springframework.http.HttpStatus;

/**
 * Catálogo de erros de negócio. Cada erro tem um código estável (o nome da constante),
 * que o front pode usar para decidir o que fazer, e uma mensagem para mostrar à pessoa.
 * As mensagens dizem o que houve e como resolver.
 */
public enum TipoErro {

    SALA_NAO_ENCONTRADA(HttpStatus.NOT_FOUND,
            "Não existe sala com o código %s. Confira o código com quem criou a sala."),
    RODADA_NAO_ENCONTRADA(HttpStatus.NOT_FOUND,
            "Esta rodada não existe. Recarregue a sala para ver a rodada atual."),
    BARALHO_INEXISTENTE(HttpStatus.NOT_FOUND,
            "O baralho %s não existe. Use FIBONACCI ou CAMISETA."),
    STATUS_INEXISTENTE(HttpStatus.BAD_REQUEST,
            "O status %s não existe no domínio %s. Consulte os valores válidos em /dominios?dominio=%2$s."),
    PARTICIPANTE_FORA_DA_SALA(HttpStatus.FORBIDDEN,
            "Você não participa desta sala. Entre na sala com um apelido para continuar."),
    APELIDO_EM_USO(HttpStatus.CONFLICT,
            "O apelido %s já está em uso nesta sala. Escolha outro."),
    CODIGO_SALA_INDISPONIVEL(HttpStatus.SERVICE_UNAVAILABLE,
            "Não conseguimos gerar um código livre para a sala. Tente criar a sala de novo."),
    OBSERVADOR_NAO_VOTA(HttpStatus.FORBIDDEN,
            "Observadores acompanham a rodada, mas não votam. Entre na sala como votante para votar."),
    CARTA_INVALIDA(HttpStatus.BAD_REQUEST,
            "A carta %s não existe no baralho %s. Escolha uma das cartas da sala."),
    RODADA_FECHADA_PARA_VOTOS(HttpStatus.CONFLICT,
            "Os votos desta rodada já foram revelados. Espere a próxima rodada para votar."),
    RODADA_JA_REVELADA(HttpStatus.CONFLICT,
            "Os votos desta rodada já foram revelados."),
    RODADA_EM_ANDAMENTO(HttpStatus.CONFLICT,
            "Já existe uma rodada em votação nesta sala. Revele os votos antes de começar outra."),
    VOTO_SIMULTANEO(HttpStatus.CONFLICT,
            "Seu voto chegou duas vezes ao mesmo tempo. Escolha a carta de novo.");

    private final HttpStatus status;
    private final String mensagem;

    TipoErro(HttpStatus status, String mensagem) {
        this.status = status;
        this.mensagem = mensagem;
    }

    public HttpStatus getStatus() { return status; }

    String formatar(Object... argumentos) {
        return mensagem.formatted(argumentos);
    }
}

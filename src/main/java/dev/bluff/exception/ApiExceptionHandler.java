package dev.bluff.exception;

import dev.bluff.controller.ApiHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(NegocioException.class)
    ProblemDetail erroNegocio(NegocioException erro) {
        return problema(erro.getTipo().getStatus(), erro.getMessage(), erro.getTipo().name());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ProblemDetail parametroInvalido(MethodArgumentTypeMismatchException erro) {
        return problema(HttpStatus.BAD_REQUEST,
                "O valor \"%s\" não é válido para %s.".formatted(erro.getValue(), erro.getName()),
                "REQUISICAO_INVALIDA");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail conflitoNoBanco(DataIntegrityViolationException erro) {
        log.warn("Violação de integridade não tratada: {}", erro.getMostSpecificCause().getMessage());
        return problema(HttpStatus.CONFLICT,
                "Os dados enviados conflitam com o que já existe. Recarregue a página e tente de novo.",
                "CONFLITO");
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail erroInesperado(Exception erro) {
        log.error("Erro não tratado", erro);
        return problema(HttpStatus.INTERNAL_SERVER_ERROR,
                "Algo falhou do nosso lado. Tente de novo em alguns instantes.",
                "ERRO_INTERNO");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException erro, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> campos = new LinkedHashMap<>();
        erro.getBindingResult().getFieldErrors()
                .forEach(campo -> campos.putIfAbsent(campo.getField(), campo.getDefaultMessage()));

        ProblemDetail problema = problema(HttpStatus.BAD_REQUEST,
                "Alguns campos não estão corretos. Confira e envie de novo.", "VALIDACAO");
        problema.setProperty("campos", campos);
        return ResponseEntity.badRequest().body(problema);
    }

    @Override
    protected ResponseEntity<Object> handleServletRequestBindingException(
            ServletRequestBindingException erro, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String detalhe = erro instanceof MissingRequestHeaderException faltando
                && ApiHeaders.PARTICIPANTE_ID.equals(faltando.getHeaderName())
                ? "Envie o cabeçalho X-Participante-Id com o id recebido ao entrar na sala."
                : "A requisição está incompleta: " + erro.getMessage();
        return ResponseEntity.badRequest().body(problema(HttpStatus.BAD_REQUEST, detalhe, "REQUISICAO_INVALIDA"));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException erro, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return ResponseEntity.badRequest().body(problema(HttpStatus.BAD_REQUEST,
                "O corpo da requisição não é um JSON válido ou tem um valor fora das opções aceitas.",
                "REQUISICAO_INVALIDA"));
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception erro, Object body, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ResponseEntity<Object> resposta = super.handleExceptionInternal(erro, body, headers, status, request);
        if (resposta != null && resposta.getBody() instanceof ProblemDetail problema
                && (problema.getProperties() == null || !problema.getProperties().containsKey("codigo"))) {
            problema.setProperty("codigo", status.is4xxClientError() ? "REQUISICAO_INVALIDA" : "ERRO_INTERNO");
        }
        return resposta;
    }

    private static ProblemDetail problema(HttpStatusCode status, String detalhe, String codigo) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setProperty("codigo", codigo);
        return problema;
    }
}

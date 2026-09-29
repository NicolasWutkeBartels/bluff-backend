package dev.bluff.exception;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;

public final class Constraints {

    public static final String SALA_CODIGO = "uk_sala_codigo";
    public static final String PARTICIPANTE_APELIDO = "uk_participante_sala_apelido";
    public static final String VOTO_RODADA_PARTICIPANTE = "uk_voto_rodada_participante";

    private Constraints() {
    }

    public static boolean violou(DataIntegrityViolationException erro, String constraint) {
        Throwable causa = erro;
        while (causa != null) {
            if (causa instanceof ConstraintViolationException violacao) {
                return constraint.equalsIgnoreCase(violacao.getConstraintName());
            }
            causa = causa.getCause();
        }
        return false;
    }
}

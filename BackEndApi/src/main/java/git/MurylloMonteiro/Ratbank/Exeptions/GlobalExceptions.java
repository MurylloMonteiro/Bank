package git.MurylloMonteiro.Ratbank.Exeptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptions {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptions.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleInvalidInput(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleUnreadableRequest(HttpMessageNotReadableException exception) {
        return ResponseEntity.badRequest().body("Não foi possível ler os dados enviados. Confira os campos e tente novamente.");
    }

    @ExceptionHandler({DataIntegrityViolationException.class, JpaSystemException.class})
    public ResponseEntity<String> handleDatabaseConflict(Exception exception) {
        log.warn("A operação foi recusada pelo banco de dados.", exception);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("Não foi possível salvar os dados. Verifique se o CPF ou e-mail já estão cadastrados.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleUnexpectedError(Exception exception) {
        log.error("Erro inesperado ao processar a solicitação.", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Ocorreu um erro inesperado. Tente novamente mais tarde.");
    }
}

package xyz.zlatanov.doyle.adapter.driving;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static org.springframework.http.HttpStatus.CONFLICT;

/**
 * Renders errors as RFC 9457 problem details. Malformed requests are handled by {@link ResponseEntityExceptionHandler}.
 */
@Slf4j
@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({ConcurrencyFailureException.class, DuplicateKeyException.class})
    ProblemDetail handleConcurrentChange(DataAccessException e) {
        log.info("Request rejected due to a concurrent change: {}", e.getMessage());
        return ProblemDetail.forStatusAndDetail(CONFLICT, "Changed by a concurrent request, retry the request");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e, HttpHeaders headers,
                                                                  HttpStatusCode status, WebRequest request) {
        e.getBody().setProperty("errors", e.getFieldErrors().stream()
                .map(error -> new InvalidField(error.getField(), error.getDefaultMessage()))
                .toList());
        return super.handleMethodArgumentNotValid(e, headers, status, request);
    }

    record InvalidField(String field, String message) {
    }
}

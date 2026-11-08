package cn.techstar.pvms.backend.common;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidBody(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getAllErrors().stream()
            .map(MessageSourceResolvable::getDefaultMessage)
            .sorted()
            .collect(Collectors.joining("；"));
        return respond(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidParameter(HandlerMethodValidationException exception) {
        String message = exception.getAllValidationResults().stream()
            .flatMap(result -> result.getResolvableErrors().stream())
            .map(MessageSourceResolvable::getDefaultMessage)
            .collect(Collectors.joining("；"));
        return respond(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadableBody(HttpMessageNotReadableException exception) {
        return respond(HttpStatus.BAD_REQUEST, "请求体格式错误");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleStatus(ResponseStatusException exception) {
        return respond(HttpStatus.valueOf(exception.getStatusCode().value()), exception.getReason());
    }

    private ResponseEntity<ApiResponse<Void>> respond(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(ApiResponse.error(status.value(), message));
    }
}

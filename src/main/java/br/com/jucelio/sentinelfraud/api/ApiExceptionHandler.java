package br.com.jucelio.sentinelfraud.api;

import br.com.jucelio.sentinelfraud.service.AssessmentNotFoundException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;

@RestControllerAdvice
public class ApiExceptionHandler {
    record ApiError(Instant timestamp,int status,String error,List<String> details) {}
    @ExceptionHandler(AssessmentNotFoundException.class) @ResponseStatus(HttpStatus.NOT_FOUND)
    ApiError notFound(AssessmentNotFoundException ex) { return new ApiError(Instant.now(),404,"Not Found",List.of(ex.getMessage())); }
    @ExceptionHandler(MethodArgumentNotValidException.class) @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiError validation(MethodArgumentNotValidException ex) {
        var details=ex.getBindingResult().getFieldErrors().stream().map(e->e.getField()+": "+e.getDefaultMessage()).toList();
        return new ApiError(Instant.now(),400,"Validation failed",details);
    }
}

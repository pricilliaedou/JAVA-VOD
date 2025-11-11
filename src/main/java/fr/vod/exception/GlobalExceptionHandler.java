package fr.vod.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

	//avec url = http://localhost:8000/bonjour
	//org.springframework.web.bind.MissingServletRequestParameterException
	@ExceptionHandler(MissingServletRequestParameterException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public APIErrorMessage handleParameterNotFoundException(MissingServletRequestParameterException ex) {
	    return new APIErrorMessage(404, "Exception levée - Vérifie le code ! ", ex.getMessage());
	}
	
	@ExceptionHandler(UtilisateurInexistantException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public APIErrorMessage handleUserNotFoundException(UtilisateurInexistantException ex) {
	    return new APIErrorMessage(404, "Pas d'utilisateur avec cet identifiant ", ex.getMessage());
	}
	
	// Validation errors from @Valid on request bodies
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<APIErrorMessage> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
	    String details = ex.getBindingResult().getFieldErrors().stream()
	            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
	            .collect(Collectors.joining("; "));
	    APIErrorMessage body = new APIErrorMessage(400, "Validation failed", details);
	    return ResponseEntity.badRequest().body(body);
	}
	
	// Validation errors from other validation mechanisms (e.g. @Validated on method params)
	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<APIErrorMessage> handleConstraintViolation(ConstraintViolationException ex) {
	    String details = ex.getConstraintViolations().stream()
	            .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
	            .collect(Collectors.joining("; "));
	    APIErrorMessage body = new APIErrorMessage(400, "Validation failed", details);
	    return ResponseEntity.badRequest().body(body);
	}
	
	//avec url = http://localhost:8000/exception500
	//getmapping pour lever une exception
	@ExceptionHandler(value = { Exception.class })
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public APIErrorMessage unknownException(Exception ex) {
		ex.printStackTrace();
        return new APIErrorMessage(500, "Erreur interne du serveur", ex.getMessage());
    }
}
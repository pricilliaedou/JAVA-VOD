package fr.vod.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
	
	//avec url = http://localhost:8000/exception500
	//getmapping pour lever une exception
	@ExceptionHandler(value = { Exception.class })
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public APIErrorMessage unknownException(Exception ex) {
		ex.printStackTrace();
        return new APIErrorMessage(500, "Erreur interne du serveur", ex.getMessage());
    }
}

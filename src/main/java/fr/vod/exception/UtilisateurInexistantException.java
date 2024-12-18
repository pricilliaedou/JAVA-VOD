package fr.vod.exception;

import lombok.Getter;

@Getter
public class UtilisateurInexistantException extends RuntimeException {

	private static final long serialVersionUID = -4001785457990592779L;
	private String message;

    public UtilisateurInexistantException() {}

    public UtilisateurInexistantException(String msg) {
        super(msg);
        this.message = msg;
    }
	
}

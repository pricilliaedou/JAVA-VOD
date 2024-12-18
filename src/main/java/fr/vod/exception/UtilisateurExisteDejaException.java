package fr.vod.exception;

import lombok.Getter;

@Getter
public class UtilisateurExisteDejaException extends RuntimeException {

	private static final long serialVersionUID = 6789520693537634957L;
	private String message;

    public UtilisateurExisteDejaException() {}

    public UtilisateurExisteDejaException(String msg) {
        super(msg);
        this.message = msg;
    }
	
}

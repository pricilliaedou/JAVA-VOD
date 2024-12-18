package fr.vod.exception;

import lombok.Getter;

@Getter
public class APIErrorMessage {

    private int status;
    private String message;
    private String detail;

    public APIErrorMessage(int status, String message, String detail) {
        this.status = status;
        this.message = message;
        this.detail = detail;
    }

}

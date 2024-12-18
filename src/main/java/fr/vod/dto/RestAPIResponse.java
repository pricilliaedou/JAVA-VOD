package fr.vod.dto;

import lombok.Getter;

@Getter
public class RestAPIResponse {
	private final int statut;
	private final String message;

	public RestAPIResponse(int statut, String token) {
		this.message = token;
		this.statut = statut;
	}
}

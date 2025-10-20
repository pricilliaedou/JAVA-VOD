package fr.vod.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor @NoArgsConstructor
public class AuthenticationResponse{
	private String token;
	private UserDTO user;
}

//public class AuthenticationResponse {
  //  private final String token;

  //  public AuthenticationResponse(String token) {
   //     this.token = token;
 //   }

//}



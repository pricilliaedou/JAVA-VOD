package fr.vod.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import fr.vod.dto.AuthenticationForm;
import fr.vod.dto.AuthenticationResponse;
import fr.vod.dto.UserDTO;  //Rajout
import fr.vod.dto.RestAPIResponse;
import fr.vod.dto.SubscribeForm;
import fr.vod.exception.UtilisateurExisteDejaException;
import fr.vod.exception.UtilisateurInexistantException;
import fr.vod.model.User;
import fr.vod.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@RestController
public class AuthController {

	@Autowired
	UserService userService;

	@PostMapping("/public/login")
	public Object login(@RequestBody AuthenticationForm loginRequest, HttpServletResponse response) {
		Authentication authenticationRequest = new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),
				loginRequest.getPassword());

		System.out.println(loginRequest.getUsername() + " / " + loginRequest.getPassword());

		User user = userService.get(loginRequest.getUsername(), loginRequest.getPassword());

		if (user != null) {
			String token = user.hashCode() + "" + System.currentTimeMillis();

			// ajouter un cookie à la reponse
			Cookie cookie = new Cookie("auth-token-vod", token);
			cookie.setHttpOnly(true);
			cookie.setSecure(true);
			  cookie.setPath("/");  //ajout
			response.addCookie(cookie);
			
			//Ajout
			UserDTO dto = new UserDTO(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName());

			// retourner directemnt le token sans redirection
			return ResponseEntity.ok(new AuthenticationResponse(token,dto));
		} else
			throw new UtilisateurInexistantException("pas d'utilisateur avec cette email en base");
	}

	@PostMapping("/public/subscribe")
	public Object subscribe(@RequestBody SubscribeForm subscribeForm, HttpServletResponse response) {
		if (!userService.exists(subscribeForm.getEmail())) {
			userService.createUser(subscribeForm.getEmail(), subscribeForm.getPassword(), subscribeForm.getLastName(),
					subscribeForm.getFirstName(), subscribeForm.getGender(), subscribeForm.getPhone());
			return ResponseEntity.ok(new RestAPIResponse(200, "Enregistrement créé avec succès"));
		} else throw new UtilisateurExisteDejaException();
		// Exception();
	}

}

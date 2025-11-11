package fr.vod.controller;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import fr.vod.dto.AuthenticationForm;
import fr.vod.dto.AuthenticationResponse;
import fr.vod.dto.UserDTO;
import fr.vod.dto.RestAPIResponse;
import fr.vod.dto.SubscribeForm;
import fr.vod.exception.UtilisateurExisteDejaException;
import fr.vod.exception.UtilisateurInexistantException;
import fr.vod.model.User;
import fr.vod.service.UserService;
import fr.vod.security.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;


@RestController
@CrossOrigin(origins = "${app.url}", allowCredentials = "true") // autorise ton front (http://localhost:5173)
public class AuthController {

    @Autowired
    UserService userService;

    @Autowired
    JwtUtil jwtUtil;

    @PostMapping("/public/login")
    public Object login(@RequestBody AuthenticationForm loginRequest, HttpServletResponse response) {

        // Auth via UserService (password checked inside)
        User user = userService.get(loginRequest.getUsername(), loginRequest.getPassword());

        if (user != null) {
            // génère un JWT
            String token = jwtUtil.generateToken(user.getEmail());

            boolean isProd = false;
            ResponseCookie authCookie = ResponseCookie.from("auth-token-vod", token)
                    .httpOnly(true)
                    .secure(isProd)                 // true en HTTPS
                    .path("/")
                    .sameSite(isProd ? "None" : "Lax")
                    .maxAge(60L * 60 * 24 * 7)      // 7 jours
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, authCookie.toString());

            UserDTO dto = new UserDTO(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName());

            return ResponseEntity.ok(new AuthenticationResponse(token, dto));
        } else {
            throw new UtilisateurInexistantException("pas d'utilisateur avec cette email en base");
        }
    }

    @GetMapping("/api/me")
    public ResponseEntity<UserDTO> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = auth.getName();
        User user = userService.findByEmail(email);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDTO dto = new UserDTO(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName());
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/public/subscribe")
    public Object subscribe(@RequestBody @Valid SubscribeForm subscribeForm, HttpServletResponse response) {
        if (!userService.exists(subscribeForm.getEmail())) {
            userService.createUser(
                    subscribeForm.getEmail(),
                    subscribeForm.getPassword(),
                    subscribeForm.getLastName(),
                    subscribeForm.getFirstName(),
                    subscribeForm.getGender(),
                    subscribeForm.getPhone()
            );
            return ResponseEntity.ok(new RestAPIResponse(200, "Enregistrement créé avec succès"));
        } else {
            throw new UtilisateurExisteDejaException();
        }
    }
}
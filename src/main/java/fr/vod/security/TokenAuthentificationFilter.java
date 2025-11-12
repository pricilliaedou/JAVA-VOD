package fr.vod.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.lang.Nullable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

import fr.vod.service.UserService;
import fr.vod.model.User;

@Component
public class TokenAuthentificationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserService userService;

    public TokenAuthentificationFilter(JwtUtil jwtUtil, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
   
        String path = request.getServletPath();
        if (path == null) return false;
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
        if (path.startsWith("/public/")) return true;
        if (path.equals("/public")) return true;
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            chain.doFilter(request, response);
            return;
        }

     
        String token = readTokenFromCookie(request, "auth-token-vod");
        if (token == null) token = readTokenFromAuthorizationHeader(request);

        boolean tokenValidated = false;
        try {
            if (token != null && jwtUtil.validateToken(token)) {
                tokenValidated = true;
                String email = jwtUtil.extractUsername(token);
                if (email != null && !email.isBlank()) {
                    User user = userService.findByEmail(email);
                    if (user != null) {
                        var authorities = List.of(new SimpleGrantedAuthority("USER"));
                        var principal = org.springframework.security.core.userdetails.User
                                .withUsername(email)
                                .password("") // pas besoin du password ici
                                .authorities(authorities)
                                .build();

                        var auth = new UsernamePasswordAuthenticationToken(principal, null, authorities);
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            }
        } catch (IllegalStateException ex) {      
            System.err.println("JWT validation failed due to missing JJWT runtime classes: " + ex.getMessage());
        }

        chain.doFilter(request, response);
    }

    @Nullable
    private String readTokenFromCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) return null;
        for (Cookie c : request.getCookies()) {
            if (cookieName.equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }

    @Nullable
    private String readTokenFromAuthorizationHeader(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}




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
        // Avoid validating JWT on public endpoints (ex: login, subscribe) and on preflight
        String path = request.getServletPath();
        if (path == null) return false;
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
        if (path.startsWith("/public/")) return true;
        // also allow exact /public
        if (path.equals("/public")) return true;
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        // If request is skipped by shouldNotFilter, this method won't be called.

        // 2) Si déjà authentifié, on continue
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            chain.doFilter(request, response);
            return;
        }

        // 3) Récupère un token depuis cookie OU header Authorization
        String token = readTokenFromCookie(request, "auth-token-vod");
        if (token == null) token = readTokenFromAuthorizationHeader(request);

        boolean tokenValidated = false;
        try {
            // 4) Si on a un token, on vérifie le JWT et on authentifie
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
            // This happens when JJWT classes are missing at runtime (JwtUtil throws informative IllegalStateException)
            // We must NOT let this break the authentication chain for public endpoints or during login flow.
            // Log the problem (System.err.println used to avoid new logging dependency here) and continue without auth.
            System.err.println("JWT validation failed due to missing JJWT runtime classes: " + ex.getMessage());
            // do not rethrow — continue without authenticating the request
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



//package fr.vod.security;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Collection;
//
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//
//@Component
//public class TokenAuthentificationFilter extends OncePerRequestFilter {
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//            throws ServletException, IOException {
//        /*final String authHeader = request.getHeader("Authorization");
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//        	System.out.println("Pas de bearer");
//            filterChain.doFilter(request, response);
//            return;
//        }
//        String token = authHeader.substring(7);*/
//    	String token = request.getParameter("token");
//        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//            if (token.equals("azertyuiop")) {
//            	UserDetails userDetails = new org.springframework.security.core.userdetails.User(
//            	        "username_a_definir",
//            	        "password_encode_de_pref",
//            	        getAuthorities());
//            	
//                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
//                        userDetails, null, userDetails.getAuthorities());
//                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//                SecurityContextHolder.getContext().setAuthentication(authToken);
//            }
//        }
//        filterChain.doFilter(request, response);
//    }
//    
//    private Collection<? extends GrantedAuthority> getAuthorities() {
//    	ArrayList<GrantedAuthority> liste = new ArrayList<GrantedAuthority>();
//    	liste.add(new SimpleGrantedAuthority("ADMIN"));
//        return liste;
//    }
//}

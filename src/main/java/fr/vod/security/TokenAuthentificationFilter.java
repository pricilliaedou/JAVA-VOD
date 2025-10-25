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

@Component
public class TokenAuthentificationFilter extends OncePerRequestFilter {

    private final TokenStore tokenStore;

    public TokenAuthentificationFilter(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        // 1) Laisse passer les préflights CORS
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        // 2) Si déjà authentifié, on continue
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            chain.doFilter(request, response);
            return;
        }

        // 3) Récupère un token depuis cookie OU header Authorization
        String token = readTokenFromCookie(request, "auth-token-vod");
        if (token == null) token = readTokenFromAuthorizationHeader(request);

        // 4) Si on a un token, on vérifie dans le store et on authentifie
        if (token != null) {
            String email = tokenStore.get(token);
            if (email != null && !email.isBlank()) {
                var authorities = List.of(new SimpleGrantedAuthority("USER"));
                var principal = org.springframework.security.core.userdetails.User
                        .withUsername(email)
                        .password("") // inutile ici
                        .authorities(authorities)
                        .build();

                var auth = new UsernamePasswordAuthenticationToken(principal, null, authorities);
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
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
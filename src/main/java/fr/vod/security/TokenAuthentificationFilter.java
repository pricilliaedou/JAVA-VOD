package fr.vod.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TokenAuthentificationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        /*final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        	System.out.println("Pas de bearer");
            filterChain.doFilter(request, response);
            return;
        }
        String token = authHeader.substring(7);*/
    	String token = request.getParameter("token");
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (token.equals("azertyuiop")) {
            	UserDetails userDetails = new org.springframework.security.core.userdetails.User(
            	        "username_a_definir",
            	        "password_encode_de_pref",
            	        getAuthorities());
            	
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
    
    private Collection<? extends GrantedAuthority> getAuthorities() {
    	ArrayList<GrantedAuthority> liste = new ArrayList<GrantedAuthority>();
    	liste.add(new SimpleGrantedAuthority("ADMIN"));
        return liste;
    }
}

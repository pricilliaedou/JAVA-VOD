package fr.vod.security;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenStore {
    private final Map<String, String> tokens = new ConcurrentHashMap<>();

    public void put(String token, String email) {
        tokens.put(token, email);
    }

    public String get(String token) {
        return tokens.get(token);
    }

    public void remove(String token) {
        tokens.remove(token);
    }
}

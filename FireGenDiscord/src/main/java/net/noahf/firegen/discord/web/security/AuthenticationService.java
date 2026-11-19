package net.noahf.firegen.discord.web.security;

import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager manager;

    public Authentication authenticate(String username, String password) {
        return manager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
    }

}

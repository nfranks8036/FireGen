package net.noahf.firegen.discord.web.security;

import lombok.AllArgsConstructor;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.users.FireGenUser;
import net.noahf.firegen.discord.users.UnauthorizedException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class FireGenAuthenticationProvider implements AuthenticationProvider {

    private final PasswordEncoder encoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        FireGenUser<?> user;
        try {
            user = Main.users.getByUsername(username, password);
        } catch (UnauthorizedException e) {
            throw new BadCredentialsException(e.getMessage(), e);
        }

        return new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities()
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}

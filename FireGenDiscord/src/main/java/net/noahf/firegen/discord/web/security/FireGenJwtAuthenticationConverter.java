package net.noahf.firegen.discord.web.security;

import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.users.FireGenUser;
import net.noahf.firegen.discord.users.UserManager;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class FireGenJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserManager users;

    public FireGenJwtAuthenticationConverter() {
        this.users = Main.users;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt source) {
        FireGenUser<?> user = users.get(source.getSubject());
        if (user == null) {
            throw new IllegalStateException("User " + source.getSubject() + " does not exist.");
        }

        return new FireGenJwtAuthenticationToken(user, source, user.getAuthorities());
    }
}

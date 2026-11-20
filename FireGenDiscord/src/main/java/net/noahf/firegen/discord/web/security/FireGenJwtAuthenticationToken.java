package net.noahf.firegen.discord.web.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;

public class FireGenJwtAuthenticationToken extends JwtAuthenticationToken {

    private final @Getter(onMethod_ = {@Override}) Object principal;

    public FireGenJwtAuthenticationToken(Object principal, Jwt jwt, Collection<? extends GrantedAuthority> authorities) {
        super(jwt, authorities);
        this.principal = principal;
    }

}

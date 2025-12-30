//package net.noahf.firegen.backend.access.auth;
//
//import org.springframework.security.authentication.AbstractAuthenticationToken;
//import org.springframework.security.core.GrantedAuthority;
//
//import java.util.Collection;
//
//public class ApiKeyAuthenticationToken extends AbstractAuthenticationToken {
//
//    private final String apiKey;
//
//    public ApiKeyAuthenticationToken(String apiKey, Collection<? extends GrantedAuthority> authorities) {
//        super(authorities);
//        this.apiKey = apiKey;
//        this.setAuthenticated(true);
//    }
//
//    @Override
//    public Object getCredentials() {
//        return null;
//    }
//
//    @Override
//    public Object getPrincipal() {
//        return this.apiKey;
//    }
//}

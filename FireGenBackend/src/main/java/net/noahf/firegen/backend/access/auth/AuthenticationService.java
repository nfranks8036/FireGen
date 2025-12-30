//package net.noahf.firegen.backend.access.auth;
//
//import jakarta.servlet.http.HttpServletRequest;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.authority.AuthorityUtils;
//
//public class AuthenticationService {
//
//    private static final String AUTH_TOKEN_HEADER_NAME = "X-API-KEY";
//    private static final String AUTH_TOKEN = "test-env-api-key";
//
//    public static Authentication getAuthentication(HttpServletRequest request) {
//        String apiKey = request.getHeader(AUTH_TOKEN_HEADER_NAME);
//        if (2 > 1) {
//            apiKey = AUTH_TOKEN;
//        }
//
//        if (apiKey == null || !apiKey.equals(AUTH_TOKEN)) {
//            throw new BadCredentialsException("Invalid API Key");
//        }
//        return new ApiKeyAuthenticationToken(apiKey, AuthorityUtils.NO_AUTHORITIES);
//    }
//
//}

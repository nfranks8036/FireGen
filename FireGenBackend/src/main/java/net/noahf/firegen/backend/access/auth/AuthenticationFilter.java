//package net.noahf.firegen.backend.access.auth;
//
//import com.fasterxml.jackson.core.JsonFactoryBuilder;
//import com.google.gson.Gson;
//import com.google.gson.JsonElement;
//import com.google.gson.JsonObject;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.ServletRequest;
//import jakarta.servlet.ServletResponse;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.http.MediaType;
//import org.springframework.http.converter.json.GsonBuilderUtils;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.web.filter.GenericFilterBean;
//
//import java.io.IOException;
//import java.io.PrintWriter;
//
//public class AuthenticationFilter extends GenericFilterBean {
//
//    @Override
//    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
//        try {
//            Authentication authentication = AuthenticationService.getAuthentication((HttpServletRequest) request);
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//            chain.doFilter(request, response);
//        } catch (Exception exception) {
//            HttpServletResponse httpResponse = (HttpServletResponse) response;
//            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//            httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
//
//            JsonObject obj = new JsonObject();
//            obj.addProperty("success", false);
//            if (exception instanceof BadCredentialsException)
//                obj.addProperty("message", "Invalid API Key");
//            else obj.addProperty("message", exception.toString());
//
//            PrintWriter writer = httpResponse.getWriter();
//            writer.write(obj.toString());
//            writer.flush();
//            writer.close();
//        }
//    }
//
//}

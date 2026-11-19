package net.noahf.firegen.discord.web;

import net.noahf.firegen.discord.users.FireGenUser;
import net.noahf.firegen.discord.web.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/aux")
public class AuxController {

    @GetMapping("/me")
    public ResponseEntity<?> getMe(Authentication authentication) {
        FireGenUser<?> user = (FireGenUser<?>) authentication.getPrincipal();
        return ApiResponse.ok(
                new LinkedHashMap<>(
                        Map.of(
                                "username", user.getName(),
                                "identifier", user.getId(),
                                "permissions", user.getPermissions()
                        )
                )
        );
    }

}

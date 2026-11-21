package net.noahf.firegen.discord.web.controllers;

import net.noahf.firegen.discord.users.FireGenUser;
import net.noahf.firegen.discord.web.api.ApiResponse;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> getMe(@AuthenticationPrincipal FireGenUser<?> user) {
        return ApiResponse.ok(
                new LinkedHashMap<>(
                        Map.of(
                                "login", user.getLoginUsername(),
                                "username", user.getUsername(),
                                "identifier", user.getId(),
                                "permissions", user.getPermissions()
                        )
                )
        );
    }

}

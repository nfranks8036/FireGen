package net.noahf.firegen.discord.web.controllers;

import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.users.FireGenUser;
import net.noahf.firegen.discord.users.Permission;
import net.noahf.firegen.discord.web.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> user(@PathVariable String id) {
        long numId;
        try {
            numId = Long.parseLong(id);
        } catch (NumberFormatException ignored) {
            return ApiResponse.fail("Expected ID to be a number, not '" + id + "'");
        }

        FireGenUser<?> user = Main.users.getById(numId);
        if (user == null) {
            return ApiResponse.fail("User does not exist: " + numId);
        }

        return ApiResponse.ok(user);
    }

    @GetMapping("/permissions")
    public ResponseEntity<ApiResponse<?>> permissions() {
        return ApiResponse.ok(Arrays.stream(Permission.values()).map(Enum::name).toList());
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(@AuthenticationPrincipal FireGenUser<?> user) {
        return ApiResponse.ok(user);
    }

}

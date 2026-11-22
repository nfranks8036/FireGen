package net.noahf.firegen.discord.web.controllers;

import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.config.files.ConfigUnits;
import net.noahf.firegen.discord.web.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agencies")
public class AgencyController {

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAgencies() {
        return ApiResponse.ok(Main.config.get(ConfigUnits.class).getAgencies().stream().sorted().toList());
    }

}

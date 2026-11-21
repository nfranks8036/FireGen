package net.noahf.firegen.discord.web.controllers;

import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.config.SingleObjectConfiguration;
import net.noahf.firegen.discord.web.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/structure")
public class StructureController {

    @GetMapping("/configs")
    public ResponseEntity<ApiResponse<?>> configs(
            @RequestParam(required = false) String file
    ) {
        if (file == null)
            return ApiResponse.ok(
                Main.config.getConfigs().stream()
                        .map(SingleObjectConfiguration::getPath)
                        .toList()
            );

        SingleObjectConfiguration<?> config = Main.config.getConfigs().stream()
                .filter(s -> s.getPath().equalsIgnoreCase(file))
                .findFirst().orElse(null);

        if (config == null) {
            return ApiResponse.fail("No config exists with path '" + file + "'");
        }

        return ApiResponse.ok(config.get());
    }

}

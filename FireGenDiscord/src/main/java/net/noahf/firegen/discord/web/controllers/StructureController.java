package net.noahf.firegen.discord.web.controllers;

import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.config.SingleObjectConfiguration;
import net.noahf.firegen.discord.users.FireGenUser;
import net.noahf.firegen.discord.users.Permission;
import net.noahf.firegen.discord.web.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/structure")
public class StructureController {

    @GetMapping("/configs")
    public ResponseEntity<ApiResponse<?>> configs(
            @RequestParam(required = false) String file
    ) {
        List<SingleObjectConfiguration<?>> configs = Main.config.getConfigs();
        if (file == null)
            return ApiResponse.ok(
                configs.stream()
                        .map(SingleObjectConfiguration::getPath)
                        .toList()
            );

        if (file.equalsIgnoreCase("all")) {
            return all(configs);
        }

        SingleObjectConfiguration<?> config = configs.stream()
                .filter(s -> s.getPath().equalsIgnoreCase(file))
                .findFirst().orElse(null);

        if (config == null) {
            return ApiResponse.fail("No config exists with path '" + file + "'");
        }

        return ApiResponse.ok(config.get());
    }

    private ResponseEntity<ApiResponse<?>> all(List<SingleObjectConfiguration<?>> configs) {
        Map<String, Object> returned = new HashMap<>();
        for (SingleObjectConfiguration<?> config : configs) {
            returned.put(config.getPath(), config.get());
        }
        return ApiResponse.ok(returned);
    }


}

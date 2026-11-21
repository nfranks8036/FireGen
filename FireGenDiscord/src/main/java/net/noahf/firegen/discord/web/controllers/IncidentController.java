package net.noahf.firegen.discord.web.controllers;

import net.noahf.firegen.api.incidents.Incident;
import net.noahf.firegen.api.incidents.status.IncidentStatus;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.services.IncidentService;
import net.noahf.firegen.discord.users.FireGenUser;
import net.noahf.firegen.discord.web.api.ApiRateLimit;
import net.noahf.firegen.discord.web.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getIncidents(
            @RequestParam(required = false) String byStatus,
            @RequestParam(required = false) String byNotStatus,
            @RequestParam(required = false) String byId
    ) {
        List<Incident> incidents = new ArrayList<>(Main.incidents.getIncidents());

        if (byStatus != null) {
            try {
                IncidentStatus status = IncidentStatus.valueOf(byStatus);

                incidents = incidents.stream().filter(i -> i.getStatus() == status).toList();
            } catch (IllegalArgumentException a) {
                return ApiResponse.fail("Not a valid status: " + byStatus);
            }
        } else if (byNotStatus != null) {
            try {
                IncidentStatus status = IncidentStatus.valueOf(byNotStatus);

                incidents = incidents.stream().filter(i -> i.getStatus() != status).toList();
            } catch (IllegalArgumentException a) {
                return ApiResponse.fail("Not a valid status: " + byNotStatus);
            }
        }

        if (byId != null) {
            incidents = incidents.stream().filter(i -> i.getFormattedId().equalsIgnoreCase(byId)).toList();
        }

        return ApiResponse.ok(incidents);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getIncident(@PathVariable String id) {
        Incident incident = Main.incidents.getIncidentBy(id);
        if (incident == null) {
            return ApiResponse.fail("No incident exist with ID '" + id + "'");
        }
        return ApiResponse.ok(incident);
    }

    @PostMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> editIncident(
            @PathVariable String id,
            @RequestBody IncidentService.IncidentEditRequest request,
            @AuthenticationPrincipal FireGenUser<?> user
    ) {
        Incident incident = Main.incidents.getIncidentBy(id);
        if (incident == null) {
            return ApiResponse.fail("No incident exist with ID '" + id + "'");
        }
        return ApiResponse.ofService(
                Main.services.incidents().editIncident(user, incident, request)
        );
    }

    @PostMapping
    @ApiRateLimit(capacity = 2, refill = 2)
    public ResponseEntity<ApiResponse<?>> createIncident(
            @RequestBody IncidentService.IncidentCreateRequest request,
            @AuthenticationPrincipal FireGenUser<?> user
    ) {
        return ApiResponse.ofService(
                Main.services.incidents().createIncident(user, request)
        );
    }


}

package net.noahf.firegen.backend.access.controllers;

import net.noahf.firegen.backend.Main;
import net.noahf.firegen.backend.database.structure.Incident;
import net.noahf.firegen.backend.database.structure.helper.IncidentSource;
import net.noahf.firegen.backend.structure.objects.IncidentType;
import net.noahf.firegen.backend.utils.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/")
public class ConstantsController {

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("incident_types")
    public ResponseEntity<?> getIncidents() {
        return ApiResponse.success(Main.st.getIncidentTypes().asNameList());
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("municipality")
    public ResponseEntity<?> getMunicipality() {
        return ApiResponse.success(Main.st.getMunicipality());
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("unit_statuses")
    public ResponseEntity<?> getUnitStatuses() {
        return ApiResponse.success(Main.st.getUnitAssignmentStatuses().asNameList());
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("radio_channels")
    public ResponseEntity<?> getRadioChannels() {
        return ApiResponse.success(Main.st.getRadioChannels().asNameList());
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("venues")
    public ResponseEntity<?> getVenues() {
        return ApiResponse.success(Main.st.getVenues().asNameList());
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("states")
    public ResponseEntity<?> getStates() {
        return ApiResponse.success(List.of("VIRGINIA"));
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("unit_types")
    public ResponseEntity<?> getUnitTypes() {
        return ApiResponse.success(Main.st.getUnitTypes().asList());
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("agencies")
    public ResponseEntity<?> getAgencies() {
        return ApiResponse.success(Main.st.getAgencies().asList());
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("caller_types")
    public ResponseEntity<?> getCallerTypes() {
        return ApiResponse.success(Arrays.stream(IncidentSource.values()).map(ct -> ct.name().replace("_", " ")).toList());
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("sources")
    public ResponseEntity<?> getSources() {
        return ApiResponse.success(Arrays.stream(IncidentSource.values()).map(is -> is.name().replace("_", " ")).toList());
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("priorities")
    public ResponseEntity<?> getPrioritiesFor(@RequestParam(required = false) String incident) {
        return ApiResponse.respond(() -> {
            if (incident == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expected parameter 'incident' with an incident type from /api/v1/incident_types");
            }
            IncidentType i = Main.st.getIncidentTypes().from(incident);
            if (i == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Incident type of '" + incident + "' not found, see list at /api/v1/incident_types");
            }
            return i.getPriorities();
        });
    }

}

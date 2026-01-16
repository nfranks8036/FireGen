package net.noahf.firegen.backend.access.controllers;

import de.danielbechler.diff.ObjectDiffer;
import de.danielbechler.diff.ObjectDifferBuilder;
import de.danielbechler.diff.node.DiffNode;
import net.noahf.firegen.backend.access.IncidentManagerService;
import net.noahf.firegen.backend.database.structure.Incident;
import net.noahf.firegen.backend.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.StringJoiner;

@RestController
@RequestMapping("/api/v1/incidents")
public class IncidentController {

    @Autowired
    IncidentManagerService incidentManagerService;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("")
    public ResponseEntity<?> getIncidents() {
        return ApiResponse.respond(() -> this.incidentManagerService.getActiveIncidents());
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/id/{id}")
    public ResponseEntity<?> getIncidentById(@PathVariable String id) {
        return ApiResponse.respond(() -> {
            Incident incident = this.incidentManagerService.getIncidentById(id);
            if (incident == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Incident not found");
            }
            return incident;
        });
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/id/{id}")
    public ResponseEntity<?> updateIncident(@PathVariable String id, @RequestBody Incident incident) {
        return ApiResponse.respond(() -> {

            Incident dummy = new Incident();
            Incident current = this.incidentManagerService.getIncidentById(id);
            ObjectDiffer differ = ObjectDifferBuilder.startBuilding()
                    .inclusion().exclude()
                        .propertyName("fullId").propertyName("incidentNumber").propertyName("incidentYear").propertyName("created").and()
                    .comparison()
                        .ofType(String.class).toUseEqualsMethod().and()
                    .build();
            DiffNode diff = differ.compare(incident, dummy);

            StringJoiner joiner = new StringJoiner(",", "Diff[", "]");
            diff.visit((node, visit) -> {
                try {
                    if (!node.isRootNode() && node.hasChanges()) {
                        joiner.add(node.getPath().toString().substring(1) + "=" + node.canonicalGet(current) + "->" + node.canonicalGet(incident));
                        node.canonicalSet(current, node.canonicalGet(incident));
                    }
                } catch (Exception exception) {
                    System.err.println("ERROR FINDING CHANGES: " + exception);
                }
            });

            this.incidentManagerService.updateIncident(id, current);

            return "Updated:" + incident.fullId + ":" + joiner.toString();
        });
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/create")
    public ResponseEntity<?> createIncident(@RequestBody Incident incident) {
        return ApiResponse.respond(() -> {
            if (this.incidentManagerService.createIncident(incident)) {
                return "Created:" + incident.fullId;
            }
            throw new IllegalStateException("Failed to insert into database, unknown reason why!");
        });
    }
}

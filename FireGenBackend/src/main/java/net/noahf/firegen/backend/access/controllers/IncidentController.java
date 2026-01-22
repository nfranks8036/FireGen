package net.noahf.firegen.backend.access.controllers;

import de.danielbechler.diff.ObjectDiffer;
import de.danielbechler.diff.ObjectDifferBuilder;
import de.danielbechler.diff.node.DiffNode;
import net.noahf.firegen.backend.access.IncidentManagerService;
import net.noahf.firegen.backend.database.structure.Incident;
import net.noahf.firegen.backend.database.structure.IncidentLogEntry;
import net.noahf.firegen.backend.database.structure.Location;
import net.noahf.firegen.backend.database.structure.helper.IncidentLogType;
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
    @PutMapping("/id/{id}/update")
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
    @PutMapping("/id/{id}/update/location")
    public ResponseEntity<?> updateLocation(@PathVariable String id, @RequestBody Location location) {
        return ApiResponse.respond(() -> {

            Location dummy = new Location();
            Incident currentIncident = this.incidentManagerService.getIncidentById(id);
            Location currentLocation = currentIncident.location;
            ObjectDiffer differ = ObjectDifferBuilder.startBuilding()
                    .comparison()
                    .ofType(String.class).toUseEqualsMethod().and()
                    .build();
            DiffNode diff = differ.compare(location, dummy);

            StringJoiner joiner = new StringJoiner(",", "Diff[", "]");
            diff.visit((node, visit) -> {
                try {
                    if (!node.isRootNode() && node.hasChanges()) {
                        joiner.add(node.getPath().toString().substring(1) + "=" + node.canonicalGet(location) + "->" + node.canonicalGet(currentLocation));
                        node.canonicalSet(currentLocation, node.canonicalGet(location));
                    }
                } catch (Exception exception) {
                    System.err.println("ERROR FINDING CHANGES: " + exception);
                }
            });

            currentIncident.location = currentLocation;
            this.incidentManagerService.updateIncident(id, currentIncident);

            return "UpdatedLocation:" + currentIncident.fullId + ":" + joiner.toString();
        });
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/id/{id}/add_narrative")
    public ResponseEntity<?> addNarrative(@PathVariable String id, @RequestBody NewNarrative narrative) {
        return ApiResponse.respond(() -> {
            Incident current = this.incidentManagerService.getIncidentById(id);

            current.log.add(IncidentLogEntry.of(IncidentLogType.NARRATIVE_ADDED, narrative.newNarrative));

            this.incidentManagerService.updateIncident(id, current);

            return "Updated:" + current.fullId + ":" + narrative.newNarrative.toString();
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

    public static class NewNarrative {
        public String newNarrative = null;
    }

}

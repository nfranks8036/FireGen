package net.noahf.firegen.backend.access.controllers;

import de.danielbechler.diff.ObjectDiffer;
import de.danielbechler.diff.ObjectDifferBuilder;
import de.danielbechler.diff.node.DiffNode;
import net.noahf.firegen.backend.Main;
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

import java.util.*;

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
    public ResponseEntity<?> updateLocation(@PathVariable String id, @RequestBody NewLocation newLocation) {
        return ApiResponse.respond(() -> {
            Incident incident = this.incidentManagerService.getIncidentById(id);
            if (incident == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Incident not found by ID '" + id + "'");
            }

            incident = newLocation.apply(incident);

            this.incidentManagerService.updateIncident(id, incident);

            return "UpdatedLocation:" + incident.fullId + ":" + incident.location.toString();
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
    @PostMapping("/new")
    public ResponseEntity<?> createIncident() {
        return ApiResponse.respond(() -> {
            Incident incident = new Incident(true);
            if (this.incidentManagerService.createIncident(incident)) {
                return "Created:" + incident.fullId;
            }
            throw new IllegalStateException("Failed to insert into database, unknown reason why!");
        });
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/count")
    public ResponseEntity<?> getNextIncident() {
        return ApiResponse.respond(() -> Main.db.countIncidents());
    }

    public static class NewNarrative {
        public String newNarrative = null;
    }

    public static class NewLocation {
        public Location.LocationType type = Location.LocationType.CUSTOM;
        public List<String> crossStreets = null;
        public String commonName = "";
        public String venue = "";
        public String state = "VA";
        public Integer zipCode = null;
        public Map<String, ?> payload = new HashMap<>();


        public Incident apply(Incident incident) {
            Location location = incident.location;
            location.primaryLocationType = this.type;
            switch (type) {
                case STREET_ADDRESS -> {
                    location.setStreetAddress(String.valueOf(this.payload.get("streetAddress")));
                }
                case MILE_MARKER -> {
                    location.setMileMarker(String.valueOf(this.payload.get("roadName")), Double.parseDouble(String.valueOf(this.payload.get("mileMarker"))));
                }
                case INTERSECTION -> {
                    location.setIntersection((ArrayList<String>)this.payload.get("intersection"));
                }
                case COORDINATES -> {
                    location.setCoordinates(Double.parseDouble(String.valueOf(this.payload.get("latitude"))), Double.parseDouble(String.valueOf(this.payload.get("longitude"))));
                }
                case CUSTOM -> {
                    location.setCustomRoad(String.valueOf(this.payload.get("custom")));
                }
            }
            if (crossStreets != null ){
                location.crossStreets = crossStreets;
            }
            if (commonName != null) {
                location.commonName = commonName;
            }
            if (venue != null) {
                location.venue = venue;
            }
            if (state != null) {
                location.state = state;
            }
            if (zipCode != null) {
                location.zipCode = zipCode;
            }
            incident.location = location;
            return incident;
        }
    }

}

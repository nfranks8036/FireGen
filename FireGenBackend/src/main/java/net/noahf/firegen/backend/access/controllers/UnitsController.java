package net.noahf.firegen.backend.access.controllers;

import dev.morphia.query.filters.Filters;
import net.noahf.firegen.backend.Main;
import net.noahf.firegen.backend.access.UnitManagerService;
import net.noahf.firegen.backend.database.structure.*;
import net.noahf.firegen.backend.database.structure.helper.AssignmentEvent;
import net.noahf.firegen.backend.database.structure.helper.IncidentLogType;
import net.noahf.firegen.backend.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/units")
public class UnitsController {

    @Autowired
    UnitManagerService unitManagerService;

    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{agency}/{unit}")
    public ResponseEntity<?> updateUnit(@PathVariable String agency, @PathVariable String unit, @RequestBody NewStatus newStatus) {
        return ApiResponse.respond(() -> {
            if (agency == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expected abbreviated agency: /api/v1/units/{agency}/{unit}");
            }
            if (unit == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expected unit type and number: /api/v1/units/{agency}/{unit}");
            }
            Agency a = Main.st.getAgencies().asList().stream().filter(ag -> ag.abbreviation.equalsIgnoreCase(agency)).findFirst().orElse(null);
            if (a == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No agency by the name of '" + agency + "', see list at /api/v1/agencies");
            }
            Unit u = a.units.stream().filter(un -> un.getCallsign().equalsIgnoreCase(unit)).findFirst().orElse(null);
            if (u == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No unit in the agency '" + agency + "' with the ID '" + unit + "'");
            }
            Incident i = Main.db.datastore().find(Incident.class).filter(Filters.eq("fullId", newStatus.incident)).first();
            if (i == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No incident exists by the ID '" + newStatus.incident + "', see active at /api/v1/incidents");
            }

            UnitAssignment ua = i.units.stream().filter(ua1 -> ua1.unit.equals(u.getCallsign())).findFirst().orElse(null);
            AssignmentEvent assign;
            if (ua == null) {
                ua = new UnitAssignment(u, i, newStatus.primary, newStatus.operate);
                assign = new AssignmentEvent(u, Main.st.getUnitAssignmentStatuses().from(newStatus.new_status), newStatus.narrative);
                ua.events.add(assign);
                u.assignment = ua;
                i.units.add(ua);
                i.log.add(IncidentLogEntry.of(IncidentLogType.UNIT_ADDED, assign.narrative));
            } else { // ua != null
                assign = new AssignmentEvent(u, Main.st.getUnitAssignmentStatuses().from(newStatus.new_status), newStatus.narrative);
                ua.events.add(assign);
                u.assignment = ua;
                i.log.add(IncidentLogEntry.of(IncidentLogType.UNIT_STATUS_UPDATED, assign.narrative));
            }

            Main.db.datastore().save(i);
            Main.db.datastore().save(u);

            return "Updated:" + u.unitId + ":" + assign.toString();
        });
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{agency}/{unit}")
    public ResponseEntity<?> getUnitStatus(@PathVariable String agency, @PathVariable String unit) {
        return ApiResponse.respond(() -> {
            if (agency == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expected abbreviated agency: /api/v1/units/{agency}/{unit}");
            }
            if (unit == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expected unit type and number: /api/v1/units/{agency}/{unit}");
            }
            Agency a = Main.st.getAgencies().asList().stream().filter(ag -> ag.abbreviation.equalsIgnoreCase(agency)).findFirst().orElse(null);
            if (a == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No agency by the name of '" + agency + "', see list at /api/v1/agencies");
            }
            Unit u = a.units.stream().filter(un -> un.getCallsign().equalsIgnoreCase(unit)).findFirst().orElse(null);
            if (u == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No unit in the agency '" + agency + "' with the ID '" + unit + "'");
            }
            return u.assignment;
        });
    }

    public static class NewStatus {
        public String incident = null;
        public boolean primary = false;
        public String operate = null;
        public String new_status = null;
        public String narrative = null;
    }

}

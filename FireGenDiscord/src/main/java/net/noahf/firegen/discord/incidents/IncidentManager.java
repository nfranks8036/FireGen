package net.noahf.firegen.discord.incidents;

import lombok.Getter;
import net.noahf.firegen.api.incidents.units.*;
import net.noahf.firegen.discord.config.ConfigManager;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.incidents.structure.location.IncidentLocationImpl;
import net.noahf.firegen.discord.incidents.structure.location.LocationVenueImpl;
import net.noahf.firegen.discord.incidents.structure.types.IncidentTypeImpl;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Represents the Incident manager, which creates, modifies, and deletes {@link IncidentImpl incidents}, as well as the
 * allowed data that can be put into the incidents, including {@link IncidentTypeImpl incident types},
 * {@link IncidentLocationImpl incident locations},
 * and even {@link LocationVenueImpl venues}.
 */
public class IncidentManager {

    /**
     * List of {@link IncidentImpl} that are currently ongoing right now. Closed incidents may still show up in this list.
     */
    private final @Getter List<net.noahf.firegen.api.incidents.Incident> incidents = new ArrayList<>();

    private final @Getter ConfigManager config;

    @Getter final Set<UnitAssignment> assignments = new LinkedHashSet<>();
    //</editor-fold>

    public IncidentManager(ConfigManager config) {
        this.config = config;
    }

    /**
     * Creates a new {@link IncidentImpl} and adds it to the {@link IncidentManager#incidents list of all active incidents}.
     * Note that this Incident will lack any data.
     * @return a blank Incident class
     */
    public IncidentImpl createNewIncident() {
        IncidentImpl incident = new IncidentImpl(this);
        this.incidents.add(incident);
        return incident;
    }

    /**
     * Retrieves an <b>ACTIVE</b> {@link IncidentImpl incident} from the manager by the {@link IncidentImpl#getId() ID}.
     * @param id the identifying number after the year in the incident number
     * @return the associated incident with the ID, or {@code null} if it's not found
     */
    public @Nullable net.noahf.firegen.api.incidents.Incident getIncidentBy(long id) {
        for (net.noahf.firegen.api.incidents.Incident i : this.incidents) {
            if (i.getId() == id) {
                return i;
            }
        }
        return null;
    }

    public int countIncidents() {
        return this.incidents.size();
    }

}

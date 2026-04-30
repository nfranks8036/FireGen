package net.noahf.firegen.discord.incidents;

import lombok.Getter;
import net.noahf.firegen.api.incidents.IncidentType;
import net.noahf.firegen.api.incidents.location.LocationVenue;
import net.noahf.firegen.api.incidents.units.Agency;
import net.noahf.firegen.api.utilities.FireGenVariables;
import net.noahf.firegen.discord.incidents.structure.AgencyImpl;
import net.noahf.firegen.discord.incidents.structure.AssignmentStatus;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.incidents.structure.IncidentTypeImpl;
import net.noahf.firegen.discord.incidents.structure.location.IncidentLocationImpl;
import net.noahf.firegen.discord.incidents.structure.location.LocationVenueImpl;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.noahf.firegen.discord.Main.MUNICIPALITY_FOLDER;

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
    private final List<net.noahf.firegen.api.incidents.Incident> incidents = new ArrayList<>();

    private final @Getter FireGenVariables fireGenVariables;

    //<editor-fold desc="Imported with IncidentStructureImporter">
    /**
     * This is the list of allowed {@link IncidentTypeImpl Incident Types}.
     * These should include every possible variant, including qualifies.
     * This is imported from the {@link FireGenVariables#incidentTypesFile()} incident types file}.
     */
    @Getter List<IncidentType> incidentTypes = new ArrayList<>();

    /**
     * This is the list of {@link AgencyImpl Agencies} in this system.
     * This is imported from the {@link FireGenVariables#agenciesFile()} agencies file}.
     */
    @Getter List<Agency> agencies = new ArrayList<>();

    /**
     * This is the list of allowed {@link LocationVenueImpl Venues}.
     * This is imported from the {@link FireGenVariables#venuesFile()} venues file}.
     */
    @Getter List<LocationVenue> venues = new ArrayList<>();

    @Getter List<AssignmentStatus> assignmentStatuses = new ArrayList<>();

    @Getter
    SystemMunicipalityImpl municipality;
    //</editor-fold>

    public IncidentManager() {
        this.fireGenVariables = FireGenVariables.createFromFolder(MUNICIPALITY_FOLDER);

        IncidentStructureImporter importer = new IncidentStructureImporter();
        importer.importIncidentTypes(this);
        importer.importAgencies(this);
        importer.importVenues(this);
        importer.importMunicipality(this);
        importer.importAssignmentStatuses(this);

        this.fireGenVariables.setVenues(this.getVenues());
    }

    /**
     * Find a certain {@link IncidentTypeImpl} given a string. This will search the
     * {@link IncidentTypeImpl#getSelectedName()} IncidentType's complete name}.
     * @param type the complete name of the incident type to search for.
     * @return the {@link IncidentTypeImpl} associated with that type, or {@code null} if not found.
     */
    public IncidentType getTypeFromString(String type) {
        for (IncidentType t : this.incidentTypes) {
            if (t.getSelectedName().equalsIgnoreCase(type)) {
                return t;
            }
        }
        return null;
    }

    /**
     * Requests the list of all allowed {@link IncidentTypeImpl IncidentTypes} and their qualifiers from the manager.
     * @return the list of allowed types
     */
    public List<IncidentType> listAllIncidentTypes() {
        return this.incidentTypes;
    }

    /**
     * Requests a list of all allowed {@link IncidentTypeImpl IncidentTypes} as {@link String strings} that are
     * autocomplete-ready.
     * @return the list of incident types which are autocomplete ready (can be fed into Discord's autocomplete). Please
     *         note: this <b>DOES NOT</b> limit the amount of results, you will receive the full list!
     */
    public List<String> listAllIncidentTypesForAutocomplete() {
        return this.listAllIncidentTypes().stream().map(IncidentType::getSelectedName).toList();
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

    /**
     * Retrieves an {@link Agency agency} from the manager by the {@link Agency#getShorthand() shorthand name}.
     * @param shorthand the shorthand for the agency name
     * @return the associated agency with that shorthand name, or {@code null} if it's not found
     */
    public @Nullable Agency getAgencyByShorthand(String shorthand) {
        for (Agency a : this.agencies) {
            if (a.getShorthand().equalsIgnoreCase(shorthand)) {
                return a;
            }
        }
        return null;
    }

    public @Nullable Agency getAgencyByLonghand(String longhand) {
        for (Agency a : this.agencies) {
            if (a.getLonghand().equalsIgnoreCase(longhand)) {
                return a;
            }
        }
        return null;
    }

    /**
     * Retrieves a {@link LocationVenueImpl venue} from the manager by the {@link LocationVenueImpl#getName() normal name}.
     * @param name the name for the venue
     * @return the associated venue with that name, or {@code null} if it's not found
     */
    public @Nullable LocationVenue getVenueBy(String name) {
        if (name == null) {
            return null;
        }

        for (LocationVenue v : this.venues) {
            if (v.getName().equalsIgnoreCase(name)) {
                return v;
            }
        }
        return null;
    }

    /**
     * Retrieves the list of {@link LocationVenueImpl venues} stringified by their name and concatenated with a ", "
     * @return the string form of the venues.
     */
    public String getConcatenatedVenues() {
        return this.venues.stream().map(LocationVenue::getName).collect(Collectors.joining(", "));
    }

    public String getStatusEmoji(net.noahf.firegen.api.incidents.Incident incident) {
        return switch (incident.getStatus()) {
            case ACTIVE -> ":green_circle:";
            case PENDING -> ":blue_circle:";
            case CLOSED, TIMED_OUT -> ":black_circle:";
        };
    }

}

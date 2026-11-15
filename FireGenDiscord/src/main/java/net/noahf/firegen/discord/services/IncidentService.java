package net.noahf.firegen.discord.services;

import net.noahf.firegen.api.Contributor;
import net.noahf.firegen.api.incidents.Incident;
import net.noahf.firegen.api.incidents.IncidentLogEntry;
import net.noahf.firegen.api.incidents.location.IncidentLocation;
import net.noahf.firegen.api.incidents.location.LocationField;
import net.noahf.firegen.api.incidents.location.LocationType;
import net.noahf.firegen.api.incidents.location.LocationVenue;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.actions.FireGenAction;
import net.noahf.firegen.discord.actions.registered.AddNarrative;
import net.noahf.firegen.discord.actions.registered.EditLocation;
import net.noahf.firegen.discord.actions.registered.Notate;
import net.noahf.firegen.discord.bot.DiscordMessages;
import net.noahf.firegen.discord.config.files.ConfigLocationPresets;
import net.noahf.firegen.discord.config.files.ConfigVenues;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.incidents.structure.location.IncidentLocationImpl;
import net.noahf.firegen.discord.users.FireGenUser;
import net.noahf.firegen.discord.users.Permission;
import net.noahf.firegen.discord.web.api.ApiResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.noahf.firegen.discord.utilities.MessageStatus.CONTENT;

@Service
public class IncidentService {

    public record IncidentCreateRequest(
            String type, String location, String units, String date, String time, String initialNarrative
    ) {  }

    public ServiceResponse<?> createIncident(FireGenUser user, IncidentCreateRequest request) {
        IncidentImpl incident = Main.incidents.createNewIncident();

        // ---------- incident type ----------
        boolean publish = false;
        String type = request.type;
        if (type.startsWith("pub-")) {
            publish = true;
            type = request.type.substring("pub-".length()).toUpperCase();
        }

        incident.setTypeBySearch(request.type);

        // ---------- incident contributors // begin list ----------
        incident.addContributor(user);
        incident.addLog(user, IncidentLogEntry.EntryType.CREATE, "New Incident: " + incident.getType().getSelectedName());

        // ---------- incident location ----------
        if (
                this.location(incident, user, request.location).) {
        }

        return ServiceResponse.success(incident);
    }

    public ServiceResponse location(Incident incident, FireGenUser user, String input) {
        if (!user.hasPermission(Permission.CHANGE_LOCATION)) {
            return ServiceResponse.fail(user, "You don't have permission to change the incident location.");
        }

        EditLocation action = findAction(EditLocation.class);

        IncidentLocation location;
        if (input.startsWith("$")) {
            location = extractLocation(input.substring(1));
        } else {
            location = Main.config.get(ConfigLocationPresets.class).fromAnyName(input);
        }

        if (location == null) {
            if (!user.hasPermission(Permission.USE_CUSTOM_LOCATION)) {
                return ServiceResponse.fail(user,
                        "You don't have permission to set a custom location, " +
                                "so the location was not set. Please use a preset, the buttons, or the " +
                                "correct custom time.");
            }

            location = new IncidentLocationImpl(List.of(input));
        }

        return action.onSubmit(incident, user, location.getType(), location);
    }

    private IncidentLocation extractLocation(String text) {
        String[] typeVsData = text.split(":");
        if (typeVsData.length != 2) {
            throw new IllegalArgumentException(
                    "Expected a LocationType and location data (such as 'LocationType:data1,data2,...'), "
                            + "got \"" + text + "\""
            );
        }

        LocationType type = LocationType.valueOf(typeVsData[0].toUpperCase());
        List<String> dataString = new ArrayList<>(Arrays.stream(typeVsData[1].split(";", -1)).toList());

        List<Integer> remove = new ArrayList<>();

        String common = null;
        LocationVenue venue = null;
        for (int i = 0; i < dataString.size(); i++) {
            if (dataString.get(i).isBlank()) {
                remove.add(i);
                continue;
            }

            LocationField field = type.getFields()[i];
            if (field.getId().equalsIgnoreCase("common-name")) {
                remove.add(i);
                common = dataString.get(i);
            }
            if (field.getId().equalsIgnoreCase("venue")) {
                remove.add(i);
                venue = Main.config.get(ConfigVenues.class).fromName(dataString.get(i));
            }
        }

        int j = 0;
        for (Integer i : remove) {
            dataString.remove(i - j);
            j++;
        }

        return new IncidentLocationImpl(dataString, type, common, venue);
    }

    public ServiceResponse narrative(Incident incident, FireGenUser user, String narrative) {
        if (!user.hasPermission(Permission.NARRATIVE_ADD)) {
            return ServiceResponse.fail(user, "You don't have permission to add to the narrative.");
        }

        if (narrative == null || narrative.isEmpty()) {
            return ServiceResponse.fail(user, "Expected narrative to contain content, found an empty text string.");
        }

        if (!narrative.toUpperCase().startsWith("NOTE:")) {
            return findAction(AddNarrative.class).onSubmit(incident, user, narrative);
        } else {
            return findAction(Notate.class).onSubmit(incident, user, narrative.substring(4));
        }
    }



    static @NotNull <T extends FireGenAction> T findAction(Class<T> clazz) {
        return Main.actions.getAction(clazz);
    }



}

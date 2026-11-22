package net.noahf.firegen.discord.services;

import net.noahf.firegen.api.incidents.Incident;
import net.noahf.firegen.api.incidents.location.IncidentLocation;
import net.noahf.firegen.api.incidents.location.LocationType;
import net.noahf.firegen.api.incidents.location.LocationVenue;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.actions.registered.EditLocation;
import net.noahf.firegen.discord.config.files.ConfigLocationTypes;
import net.noahf.firegen.discord.config.files.ConfigVenues;
import net.noahf.firegen.discord.incidents.structure.location.IncidentLocationImpl;
import net.noahf.firegen.discord.users.FireGenUser;
import net.noahf.firegen.discord.users.Permission;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IncidentLocationService {

    public record IncidentLocationUpdate(
            List<String> data, String type, String commonName, String venue
    ) {  }

    public ServiceResponse<?> editLocation(FireGenUser<?> user, Incident incident, IncidentLocationUpdate request) {
        if (!user.hasPermission(Permission.CHANGE_LOCATION)) {
            return ServiceResponse.fail(user, "You don't have permission to change the incident location!");
        }

        IncidentLocation location;

        LocationType type = Main.config.get(ConfigLocationTypes.class).fromId(request.type);
        if (type == null) {
            return ServiceResponse.fail(user, "Invalid location type: " + request.type);
        }

        if (type.isFallback() && !user.hasPermission(Permission.USE_CUSTOM_LOCATION)) {
            return ServiceResponse.fail(user, "You don't have permission to use a custom incident location!");
        }

        LocationVenue venue = Main.config.get(ConfigVenues.class).fromName(request.venue);
        if (request.venue != null && venue == null) {
            return ServiceResponse.fail(user, "Invalid venue: " + request.venue);
        }

        location = new IncidentLocationImpl(
                request.data, type, request.commonName, venue
        );
        Main.actions.getAction(EditLocation.class).onSubmit(incident, user, type, location);

        incident.addContributor(user);
        incident.update();

        return ServiceResponse.success(location);
    }

}

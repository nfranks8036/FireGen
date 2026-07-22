package net.noahf.firegen.discord.config.files;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.noahf.firegen.api.incidents.location.LocationVenue;
import net.noahf.firegen.api.utilities.FireGenVariables;
import net.noahf.firegen.discord.config.DependencyRequest;
import net.noahf.firegen.discord.config.MultiObjectConfiguration;
import net.noahf.firegen.discord.incidents.structure.location.LocationVenueImpl;
import net.noahf.firegen.discord.utilities.Log;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Collectors;

import static net.noahf.firegen.discord.utilities.JsonUtilities.asStr;

public class ConfigVenues extends MultiObjectConfiguration<LocationVenue> {

    public ConfigVenues(FireGenVariables vars) {
        super(vars, LocationVenue.class, vars.municipality() + "/" + vars.venuesFile());
    }

    @Override
    public void importObject(JsonElement e) {
        JsonArray array = e.getAsJsonArray();
        for (JsonElement element : array.asList()) {
            JsonObject object = element.getAsJsonObject();
            String name = asStr(object, "name");
            String display = asStr(object, "display");

            this.add(new LocationVenueImpl(name, display));
        }

        this.getVars().setVenues(this.get());
        Log.info("Imported venues " + String.join(", ", this.get()));
    }

    /**
     * Retrieves a {@link LocationVenueImpl venue} from the manager by the {@link LocationVenueImpl#getName() normal name}.
     * @param name the name for the venue
     * @return the associated venue with that name, or {@code null} if it's not found
     */
    public @Nullable LocationVenue fromName(String name) {
        if (name == null) {
            return null;
        }

        for (LocationVenue v : this.get()) {
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
    public String asUnifiedString() {
        return this.get().stream().map(LocationVenue::getName).collect(Collectors.joining(", "));
    }
}

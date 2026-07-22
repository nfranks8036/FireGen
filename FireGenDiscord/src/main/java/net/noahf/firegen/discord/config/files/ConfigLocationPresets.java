package net.noahf.firegen.discord.config.files;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.noahf.firegen.api.incidents.location.IncidentLocation;
import net.noahf.firegen.api.utilities.FireGenVariables;
import net.noahf.firegen.discord.config.DependencyRequest;
import net.noahf.firegen.discord.config.MultiObjectConfiguration;
import net.noahf.firegen.discord.incidents.structure.location.IncidentLocationImpl;
import net.noahf.firegen.discord.incidents.structure.location.LocationPreset;
import net.noahf.firegen.discord.utilities.Log;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class ConfigLocationPresets extends MultiObjectConfiguration<LocationPreset> {

    public ConfigLocationPresets(FireGenVariables vars) {
        super(vars, LocationPreset.class, vars.municipality() + "/" + vars.locationPresetsFile(),
                new DependencyRequest().dependOn(ConfigVenues.class)
        );
    }

    @Override
    public void importObject(JsonElement e) {
        JsonObject root = e.getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
            String key = entry.getKey();
            JsonObject value = entry.getValue().getAsJsonObject();

            LocationPreset preset = new LocationPreset(key, value, this.getDependencies().get(ConfigVenues.class));
            this.add(preset);
        }

        Log.info("Imported " + this.count() + " preset locations.");
    }

    public List<String> asAutocompleteStrings() {
        return Stream.concat(
                        this.get().stream().map(IncidentLocationImpl::getCommonName).filter(Objects::nonNull),
                        this.get().stream().map(lp -> String.join(" ", lp.getData()))
                )
                .toList();
    }

    public @Nullable IncidentLocation fromAnyName(String text) {
        return this.get().stream()
                .filter(lp ->
                        (lp.getCommonName() != null ? lp.getCommonName() : "").equalsIgnoreCase(text)
                                || String.join(" ", lp.getData()).equalsIgnoreCase(text)
                )
                .findFirst().orElse(null);
    }
}

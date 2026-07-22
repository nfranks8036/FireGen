package net.noahf.firegen.discord.config.files;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.noahf.firegen.api.incidents.status.IncidentStatus;
import net.noahf.firegen.api.utilities.FireGenVariables;
import net.noahf.firegen.discord.config.MultiObjectConfiguration;
import net.noahf.firegen.discord.incidents.structure.IncidentStatusEmoji;
import net.noahf.firegen.discord.utilities.Log;

import static net.noahf.firegen.discord.utilities.JsonUtilities.asStr;

public class ConfigIncidentStatuses extends MultiObjectConfiguration<IncidentStatusEmoji> {

    public ConfigIncidentStatuses(FireGenVariables vars) {
        super(vars, IncidentStatusEmoji.class, vars.incidentStatusFile());
    }

    @Override
    public void importObject(JsonElement e) {
        JsonArray array = e.getAsJsonArray();
        for (JsonElement element : array.asList()) {
            JsonObject object = element.getAsJsonObject();

            String name = asStr(object, "name");
            String leftEmoji = asStr(object, "emojiLeft");
            String rightEmoji = asStr(object, "emojiRight");

            this.add(new IncidentStatusEmoji(name, leftEmoji, rightEmoji));
        }

        Log.info("Imported incident statuses " + String.join(", ", this.get()));
    }

    public IncidentStatusEmoji asEmoji(IncidentStatus status) {
        for (IncidentStatusEmoji e : this.get()) {
            if (e.getStatus() == status) {
                return e;
            }
        }
        return null;
    }
}

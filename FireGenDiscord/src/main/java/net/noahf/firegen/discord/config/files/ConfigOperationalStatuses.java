package net.noahf.firegen.discord.config.files;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.noahf.firegen.api.incidents.units.*;
import net.noahf.firegen.api.utilities.FireGenVariables;
import net.noahf.firegen.discord.config.MultiObjectConfiguration;
import net.noahf.firegen.discord.incidents.structure.units.OperationalStatusImpl;
import net.noahf.firegen.discord.utilities.ansi.AnsiColor;

import java.util.HashMap;
import java.util.Map;

import static net.noahf.firegen.discord.utilities.JsonUtilities.asStr;
import static net.noahf.firegen.discord.utilities.JsonUtilities.element;

public class ConfigOperationalStatuses extends MultiObjectConfiguration<OperationalStatus> {

    public ConfigOperationalStatuses(FireGenVariables vars) {
        super(vars, OperationalStatus.class, vars.municipality() + "/" + vars.operationalStatusFile());
    }

    @Override
    public void importObject(JsonElement element) {
        JsonArray array = element(element.getAsJsonObject(), "statuses", false).getAsJsonArray();
        int index = 0;
        for (JsonElement e : array.asList()) {
            JsonObject obj = e.getAsJsonObject();

            String name = asStr(obj, "name", false);
            String shortName = asStr(obj, "short", false);
            String emojiStr = asStr(obj, "emoji", false);
            AnsiColor ansi = element(obj, "ansi", false, (f) -> AnsiColor.valueOf(f.getAsString()));
            OperationalStatusPurpose purpose = element(obj, "purpose", true, (f) -> OperationalStatusPurpose.valueOf(f.getAsString()));

            OperationalStatus status = new OperationalStatusImpl(
                    name, shortName, index, purpose, ansi, emojiStr, null
            );
            this.add(status);

            index++;
        }

        log("Imported unit statuses " + String.join(", ", this.get()));
    }

    public OperationalStatus fromName(String name) {
        for (OperationalStatus status : this.get()) {
            if (status.getName().equalsIgnoreCase(name)) {
                return status;
            }
        }
        return null;
    }

    public OperationalStatus fromShortName(String shortName) {
        for (OperationalStatus status : this.get()) {
            if (status.getShortName().equalsIgnoreCase(shortName)) {
                return status;
            }
        }
        return null;
    }

    private final Map<OperationalStatusPurpose, OperationalStatus> purposeToStatus = new HashMap<>();

    public OperationalStatus purposeIs(OperationalStatusPurpose purpose) {
        OperationalStatus storedStatus = purposeToStatus.get(purpose);
        if (storedStatus != null) {
            return storedStatus;
        }

        for (OperationalStatus status : this.get()) {
            if (status.getPurpose() == purpose) {
                purposeToStatus.put(purpose, status);
                return status;
            }
        }

        throw new IllegalStateException("No operational statuses were imported with the purpose of '" + purpose.name() + "'");

    }
}

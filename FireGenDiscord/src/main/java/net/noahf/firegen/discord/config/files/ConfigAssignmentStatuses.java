package net.noahf.firegen.discord.config.files;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.noahf.firegen.api.incidents.units.AssignmentPurpose;
import net.noahf.firegen.api.incidents.units.AssignmentStatus;
import net.noahf.firegen.api.utilities.FireGenVariables;
import net.noahf.firegen.discord.config.MultiObjectConfiguration;
import net.noahf.firegen.discord.incidents.structure.units.AssignmentStatusImpl;
import net.noahf.firegen.discord.utilities.JsonUtilities;
import net.noahf.firegen.discord.utilities.Log;
import net.noahf.firegen.discord.utilities.ansi.AnsiColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.noahf.firegen.discord.utilities.JsonUtilities.asStr;

public class ConfigAssignmentStatuses extends MultiObjectConfiguration<AssignmentStatus> {

    public ConfigAssignmentStatuses(FireGenVariables vars) {
        super(vars, AssignmentStatus.class, vars.municipality() + "/" + vars.assignmentStatusFile());
    }

    @Override
    public void importObject(JsonElement e) {
        JsonObject root = e.getAsJsonObject();

        JsonArray tagsArray = root.getAsJsonArray("tags");
        Map<String, List<String>> tags = new HashMap<>();
        for (JsonElement element : tagsArray.asList()) {
            JsonObject object = element.getAsJsonObject();

            String name = object.get("name").getAsString();
            JsonArray secondariesArray = object.get("secondaries").getAsJsonArray();

            tags.put(name, secondariesArray.asList().stream()
                    .map(JsonElement::getAsString)
                    .toList()
            );
        }

        JsonArray statuses = root.getAsJsonArray("statuses");
        this.addAll(List.of(AssignmentStatusImpl.REMOVE_UNIT, AssignmentStatusImpl.ADD_UNIT));
        for (int i = 0; i < statuses.asList().size(); i++) {
            JsonElement element = statuses.asList().get(i);
            JsonObject object = element.getAsJsonObject();

            String name = asStr(object, "name");
            String shortName = asStr(object, "short");
            String emojiStr = asStr(object, "emoji");
            Emoji emoji = Emoji.fromFormatted(emojiStr);
            String ansiStr = asStr(object, "ansi");
            AnsiColor ansi = AnsiColor.valueOf(ansiStr.toUpperCase());
            JsonElement purposeElement = JsonUtilities.element(object, "purpose", true);
            AssignmentPurpose purpose = purposeElement != null ? AssignmentPurpose.valueOf(purposeElement.getAsString()) : null;

            JsonElement secondaryTagElement = JsonUtilities.element(object, "tag", true);
            List<String> secondaries = secondaryTagElement != null ?
                    tags.getOrDefault(secondaryTagElement.getAsString(), new ArrayList<>()) :
                    new ArrayList<>();

            AssignmentStatusImpl status = new AssignmentStatusImpl(name, shortName, emoji, new AnsiColor[]{ansi}, i, purpose, secondaries);
            this.add(status);
        }

        Log.info("Imported assignment statuses " + String.join(", ", this.get()));
    }

    public AssignmentStatus getAssignmentStatusByShortName(String shortName) {
        for (AssignmentStatus status : this.get()) {
            if (status.getShortName().equalsIgnoreCase(shortName)) {
                return status;
            }
        }
        return null;
    }
}

package net.noahf.firegen.discord.config.files;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.noahf.firegen.api.incidents.units.AssignmentPurpose;
import net.noahf.firegen.api.incidents.units.AssignmentStatus;
import net.noahf.firegen.api.incidents.units.Secondary;
import net.noahf.firegen.api.incidents.units.UnitStatus;
import net.noahf.firegen.api.utilities.FireGenVariables;
import net.noahf.firegen.discord.config.MultiObjectConfiguration;
import net.noahf.firegen.discord.incidents.structure.units.AssignmentStatusImpl;
import net.noahf.firegen.discord.incidents.structure.units.SecondaryImpl;
import net.noahf.firegen.discord.utilities.ansi.AnsiColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.noahf.firegen.discord.utilities.JsonUtilities.asStr;
import static net.noahf.firegen.discord.utilities.JsonUtilities.element;

public class ConfigUnitStatuses extends MultiObjectConfiguration<UnitStatus> {

    public ConfigUnitStatuses(FireGenVariables vars) {
        super(vars, UnitStatus.class, vars.municipality() + "/" + vars.assignmentStatusFile());
    }

    @Override
    public void importObject(JsonElement element) {
        JsonObject root = element.getAsJsonObject();
        log("Imported assignment statuses " + String.join(", ", this.get()));
    }
}

package net.noahf.firegen.discord.config.files;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.noahf.firegen.api.incidents.units.Agency;
import net.noahf.firegen.api.incidents.units.AgencyType;
import net.noahf.firegen.api.incidents.units.Unit;
import net.noahf.firegen.api.utilities.FireGenVariables;
import net.noahf.firegen.discord.config.MultiObjectConfiguration;
import net.noahf.firegen.discord.incidents.structure.units.AgencyImpl;
import net.noahf.firegen.discord.incidents.structure.units.UnitImpl;
import net.noahf.firegen.discord.utilities.JsonUtilities;
import net.noahf.firegen.discord.utilities.Log;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.noahf.firegen.discord.utilities.JsonUtilities.asStr;
import static net.noahf.firegen.discord.utilities.JsonUtilities.element;

public class ConfigUnits extends MultiObjectConfiguration<Unit> {

    private @Getter List<Agency> agencies;

    public ConfigUnits(FireGenVariables vars) {
        super(vars, Unit.class, vars.municipality() + "/" + vars.unitsFile());
        this.agencies = new ArrayList<>();
    }

    @Override
    public void importObject(JsonElement e) {
        JsonArray array = e.getAsJsonArray();

        List<JsonElement> agencyElements = array.asList();
        int lastUnitCount = 0;
        for (int i = 0; i < agencyElements.size(); i++) {
            JsonObject agencyObj = agencyElements.get(i).getAsJsonObject();

            JsonElement element = agencyObj.get("emoji");
            Emoji emoji = element != null && !element.isJsonNull() ? Emoji.fromFormatted(element.getAsString()) : null;
            Agency agency = new AgencyImpl(
                    asStr(agencyObj, "title"),
                    asStr(agencyObj, "short"),
                    asStr(agencyObj, "format"),
                    asStr(agencyObj, "station"),
                    AgencyType.valueOf(asStr(agencyObj, "type")),
                    emoji,
                    i,
                    new ArrayList<>(),
                    lastUnitCount
            );

            List<JsonElement> unitElements = element(agencyObj, "units").getAsJsonArray().asList();
            for (int j = 0; j < unitElements.size(); j++) {
                JsonObject unitObj = unitElements.get(j).getAsJsonObject();

                JsonElement unitEmojiElement = JsonUtilities.element(unitObj, "emoji", true);
                Emoji unitEmoji = unitEmojiElement != null ? Emoji.fromFormatted(unitEmojiElement.getAsString()) : emoji;
                String longhand = asStr(unitObj, "long");
                String shorthand = asStr(unitObj, "short");

                Unit unit = new UnitImpl(
                        shorthand,
                        longhand,
                        asStr(unitObj, "format"),
                        unitEmoji,
                        agency,
                        lastUnitCount + j,
                        false,
                        SelectOption.of(longhand, shorthand)
                                .withDescription(null)
                                .withEmoji(emoji)
                );

                agency.getUnits().add(unit);
            }

            this.addAll(agency.getUnits());
            this.agencies.add(agency);

            lastUnitCount = lastUnitCount + agency.getUnits().size();
        }

        List<Agency> agencies = this.agencies.reversed();
        for (int i = 0; i < agencies.size(); i++) {
            Agency agency = agencies.get(i);
            this.get().addFirst(
                    new UnitImpl(agency.getShorthand(), agency.getTitle(), agency.getFormatted(),
                            ((AgencyImpl)agency).getEmoji(), agency, Integer.MIN_VALUE + i, true,
                            SelectOption.of(agency.getTitle(), agency.getShorthand())
                                    .withDescription(null)
                                    .withEmoji(((AgencyImpl)agency).getEmoji())
                    )
            );
        }

        Log.info("Imported " + this.count() + " units (" + agencies.size() + " agencies).");
    }

    @Override
    protected void clear() {
        super.clear();
        this.agencies = new ArrayList<>();
    }

    public @Nullable Unit getUnitByShorthand(String shorthand) {
        for (Unit a : this.get()) {
            if (a.getShorthand().equalsIgnoreCase(shorthand)) {
                return a;
            }
        }
        return null;
    }

    public @Nullable Unit getUnitByLonghand(String longhand) {
        for (Unit a : this.get()) {
            if (a.getLonghand().equalsIgnoreCase(longhand)) {
                return a;
            }
        }
        return null;
    }

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
            if (a.getTitle().equalsIgnoreCase(longhand)) {
                return a;
            }
        }
        return null;
    }
}

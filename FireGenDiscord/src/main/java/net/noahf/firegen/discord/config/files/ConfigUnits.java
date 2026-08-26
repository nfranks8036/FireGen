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
import net.noahf.firegen.api.incidents.units.UnitType;
import net.noahf.firegen.api.utilities.FireGenVariables;
import net.noahf.firegen.discord.config.DependencyRequest;
import net.noahf.firegen.discord.config.MultiObjectConfiguration;
import net.noahf.firegen.discord.incidents.structure.units.AgencyImpl;
import net.noahf.firegen.discord.incidents.structure.units.UnitImpl;
import net.noahf.firegen.discord.incidents.structure.units.UnitTypeImpl;
import net.noahf.firegen.discord.utilities.JsonUtilities;
import net.noahf.firegen.discord.utilities.IntList;
import net.noahf.firegen.discord.utilities.Log;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.noahf.firegen.discord.utilities.JsonUtilities.*;

public class ConfigUnits extends MultiObjectConfiguration<Unit> {

    private @Getter List<Agency> agencies;

    public ConfigUnits(FireGenVariables vars) {
        super(vars, Unit.class, vars.municipality() + "/" + vars.unitsFile(),
                new DependencyRequest().dependOn(ConfigUnitTypes.class)
        );
        this.agencies = new ArrayList<>();
    }

    @Override
    public void importObject(JsonElement e) {
        JsonArray array = e.getAsJsonArray();
        ConfigUnitTypes types = this.getDependencies().get(ConfigUnitTypes.class);

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
            this.findUnits(unitElements, types, lastUnitCount, agency, emoji);

            this.addAll(agency.getUnits());
            this.agencies.add(agency);

            lastUnitCount = lastUnitCount + agency.getUnits().size();
        }

        List<Agency> agencies = this.agencies.reversed();
        for (int i = 0; i < agencies.size(); i++) {
            Agency agency = agencies.get(i);
            this.get().addFirst(
                    new UnitImpl(agency.getShorthand(), agency.getTitle(), agency.getFormatted(),
                            ((AgencyImpl)agency).getEmoji(), agency, UnitTypeImpl.AGENCY,
                            Integer.MIN_VALUE, Integer.MIN_VALUE + i, null, true,
                            SelectOption.of(agency.getTitle(), agency.getShorthand())
                                    .withDescription(null)
                                    .withEmoji(((AgencyImpl)agency).getEmoji())
                    )
            );
        }

        log("Imported " + this.count() + " units (" + agencies.size() + " agencies).");
    }

    private void findUnits(List<JsonElement> unitElements, ConfigUnitTypes types, int lastUnitCount, Agency agency, Emoji emoji) {
        for (int j = 0; j < unitElements.size(); j++) {
            JsonObject unitObj = unitElements.get(j).getAsJsonObject();

            JsonElement unitEmojiElement = JsonUtilities.element(unitObj, "emoji", true);
            Emoji unitEmoji = unitEmojiElement != null ? Emoji.fromFormatted(unitEmojiElement.getAsString()) : emoji;

            String typeStr = asStr(unitObj, "type", true);
            UnitType type = typeStr != null ?  types.fromId(typeStr) : UnitTypeImpl.CUSTOM;
            if (type == null) {
                throw new IllegalArgumentException("Entered 'type' field but type is not valid \"" + typeStr + "\". Valid fields are: " + types.get());
            }


            JsonElement numberElement = element(unitObj, "number", true);
            int number = Integer.MIN_VALUE;
            if (numberElement != null) {
                number = numberElement.getAsInt();
            }

            String longhand = agency.getShorthand() + " " + (type.getLonghand() != null ? type.asLonghand(number) : String.valueOf(number));
            String shorthand = type.getShorthand() != null ? type.asShorthand(number) : String.valueOf(number);
            String formatted = type.getFormatted() != null ? type.asFormatted(number) : longhand;

            longhand = asStr(unitObj, "long", longhand);
            shorthand = asStr(unitObj, "short", shorthand);
            formatted = asStr(unitObj, "format", formatted);

            JsonElement placeholdersElement = JsonUtilities.element(unitObj, "placeholders", true);
            IntList intList = null;
            if (placeholdersElement != null && !placeholdersElement.isJsonNull()) {
                Map<String, String> placeholders = placeholdersElement.getAsJsonObject()
                        .asMap()
                        .entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, c -> c.getValue().getAsString()));
                for (Map.Entry<String, String> s : placeholders.entrySet()) {
                    if (s.getValue().contains("IntList")) {
                        intList = new IntList(s.getKey(), s.getValue());
                        continue;
                    }

                    longhand = longhand.replace(s.getKey(), s.getValue());
                    shorthand = shorthand.replace(s.getKey(), s.getValue());
                    formatted = formatted.replace(s.getKey(), s.getValue());
                }
            }

            Unit unit = new UnitImpl(
                    shorthand, longhand, formatted, unitEmoji, agency,
                    type, number,
                    lastUnitCount + j, intList, false,
                    SelectOption.of(longhand, shorthand)
                            .withDescription(null)
                            .withEmoji(emoji)
            );

            agency.getUnits().add(unit);
        }
    }

    @Override
    protected void clear() {
        super.clear();
        this.agencies = new ArrayList<>();
    }

    public @Nullable Unit fromShorthand(String shorthand) {
        // check for an exact match (most common way for units to match, so it will loop this first)
        for (Unit a : this.get()) {
            if (a.getShorthand().equalsIgnoreCase(shorthand)) {
                return a;
            }
        }

        // it's possible the unit is a custom one like this
        for (Unit a : this.get()) {
            Unit c = checkAdditional(a, a.getShorthand(), shorthand);
            if (c != null) return c;
        }

        return null;
    }

    public @Nullable Unit fromLonghand(String longhand) {
        for (Unit a : this.get()) {
            if (a.getLonghand().equalsIgnoreCase(longhand)) {
                return a;
            }
        }

        for (Unit a : this.get()) {
            Unit c = checkAdditional(a, a.getLonghand(), longhand);
            if (c != null) return c;
        }

        return null;
    }

    private Unit checkAdditional(Unit parent, String format, String input) {
        if (parent.getAdditional() == null) return null;
        if (!(parent.getAdditional() instanceof IntList intList)) return null;

        int pos = format.indexOf(intList.getKey());
        String prefix = format.substring(0, pos);
        String suffix = format.substring(pos+1);
        if (!input.startsWith(prefix) && (suffix.isEmpty() || !input.endsWith(suffix))) return null;

        String numberStr = input.substring(prefix.length(), input.length() - suffix.length());
        int number = Integer.parseInt(numberStr.strip());

        return add(
                ((AgencyImpl)parent.getAgency()).newUnit(
                        intList.createUnit(parent, number)
                )
        );
    }

    public @Nullable Agency agencyFromShorthand(String shorthand) {
        for (Agency a : this.agencies) {
            if (a.getShorthand().equalsIgnoreCase(shorthand)) {
                return a;
            }
        }
        return null;
    }

    public @Nullable Agency agencyFromLonghand(String longhand) {
        for (Agency a : this.agencies) {
            if (a.getTitle().equalsIgnoreCase(longhand)) {
                return a;
            }
        }
        return null;
    }
}

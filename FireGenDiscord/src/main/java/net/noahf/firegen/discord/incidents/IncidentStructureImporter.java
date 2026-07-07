package net.noahf.firegen.discord.incidents;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.noahf.firegen.api.incidents.types.IncidentType;
import net.noahf.firegen.api.incidents.types.IncidentTypeTag;
import net.noahf.firegen.api.incidents.units.*;
import net.noahf.firegen.api.utilities.FireGenVariables;
import net.noahf.firegen.discord.incidents.structure.*;
import net.noahf.firegen.discord.incidents.structure.location.LocationPreset;
import net.noahf.firegen.discord.incidents.structure.location.LocationVenueImpl;
import net.noahf.firegen.discord.incidents.structure.types.IncidentTypeImpl;
import net.noahf.firegen.discord.incidents.structure.types.IncidentTypeTagImpl;
import net.noahf.firegen.discord.incidents.structure.units.AgencyImpl;
import net.noahf.firegen.discord.incidents.structure.units.AssignmentStatusImpl;
import net.noahf.firegen.discord.incidents.structure.units.RadioChannelImpl;
import net.noahf.firegen.discord.incidents.structure.units.UnitImpl;
import net.noahf.firegen.discord.utilities.JsonUtilities;
import net.noahf.firegen.discord.utilities.Log;
import net.noahf.firegen.discord.utilities.ansi.AnsiColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.noahf.firegen.discord.utilities.JsonUtilities.*;

public class IncidentStructureImporter {

    void importIncidentTypes(IncidentManager manager) {
        FireGenVariables vars = manager.getFireGenVariables();
        final String NEW_INCIDENT = "NEW_INCIDENT";
        JsonUtilities.stream(vars.municipality(), vars.incidentTypesFile(), (e) -> {
            JsonObject object = e.getAsJsonObject();

            List<IncidentTypeTagImpl> tags = new ArrayList<>();
            IncidentTypeTagImpl newTag = null;
            for (JsonElement element : object.getAsJsonArray("tags").asList()) {
                IncidentTypeTagImpl tag = new IncidentTypeTagImpl(element.getAsJsonObject());
                tags.add(tag);
                if (tag.tagName.equalsIgnoreCase(NEW_INCIDENT)) {
                    newTag = tag;
                }
            }
            if (newTag == null) {
                throw new IllegalStateException("Expected an incident tag with name '" + NEW_INCIDENT + "', found none.");
            }

            for (JsonElement element : object.getAsJsonArray("types").asList()) {
                JsonObject obj = element.getAsJsonObject();
                String name = asStr(obj, "name");
                String tagStr = asStr(obj, "tag");
                IncidentTypeTag tag = tags.stream().filter(itt -> itt.tagName.equalsIgnoreCase(tagStr)).findFirst().orElse(null);
                if (tag == null) {
                    throw new IllegalStateException("Expected type '" + name + "' to have an associated 'tag'");
                }
                List<IncidentType> types = new ArrayList<>();
                if (tagStr.equalsIgnoreCase(NEW_INCIDENT)) {
                    vars.defaultType(new IncidentTypeImpl(name, tag, 0));
                    types.add(vars.defaultType());
                } else if (tag.getQualifiers() == null) {
                    types.add(new IncidentTypeImpl(name, tag, 0));
                } else {
                    List<String> stringTags = tag.findTypeOptions(name);
                    for (int i = 0; i < stringTags.size(); i++) {
                        types.add(new IncidentTypeImpl(name, tag, i));
                    }
                }

                manager.incidentTypes.addAll(types);
            }

            if (vars.defaultType() == null) {
                throw new IllegalStateException("Expected an incident type to be tagged '" + NEW_INCIDENT + "', found none.");
            }

            Log.info("Imported " + manager.incidentTypes.size() + " incident types.");
        });
    }

    void importUnits(IncidentManager manager) {
        FireGenVariables vars = manager.getFireGenVariables();
        JsonUtilities.stream(vars.municipality(), vars.unitsFile(), (e) -> {
            JsonArray array = e.getAsJsonArray();

            List<JsonElement> agencyElements = array.asList();
            int lastUnitCount = 0;
            for (int i = 0; i < agencyElements.size(); i++) {
                JsonObject agencyObj = agencyElements.get(i).getAsJsonObject();

                Emoji emoji = Emoji.fromFormatted(agencyObj.get("emoji").getAsString());
                Agency agency = new AgencyImpl(
                        asStr(agencyObj, "title"),
                        asStr(agencyObj, "short"),
                        asStr(agencyObj, "format"),
                        asStr(agencyObj, "station"),
                        AgencyType.valueOf(asStr(agencyObj, "type")),
                        emoji,
                        i,
                        new ArrayList<>()
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

                manager.units.addAll(agency.getUnits());
                manager.agencies.add(agency);

                lastUnitCount = lastUnitCount + agency.getUnits().size();
            }

            List<Agency> agencies = manager.agencies.reversed();
            for (int i = 0; i < agencies.size(); i++) {
                Agency agency = agencies.get(i);
                manager.units.addFirst(
                        new UnitImpl(agency.getShorthand(), agency.getTitle(), agency.getFormatted(),
                                ((AgencyImpl)agency).getEmoji(), agency, Integer.MIN_VALUE + i, true,
                                SelectOption.of(agency.getTitle(), agency.getShorthand())
                                        .withDescription(null)
                                        .withEmoji(((AgencyImpl)agency).getEmoji())
                        )
                );
            }

            Log.info("Imported " + manager.units.size() + " units (" + agencies.size() + " agencies).");
        });
    }

    void importVenues(IncidentManager manager) {
        FireGenVariables vars = manager.getFireGenVariables();
        JsonUtilities.stream(vars.municipality(), vars.venuesFile(), (e) -> {
            JsonArray array = e.getAsJsonArray();
            for (JsonElement element : array.asList()) {
                JsonObject object = element.getAsJsonObject();
                String name = asStr(object, "name");
                String display = asStr(object, "display");

                manager.venues.add(new LocationVenueImpl(name, display));
            }

            vars.setVenues(manager.venues);
            Log.info("Imported venues " + String.join(", ", manager.venues));
        });
    }

    void importMunicipality(IncidentManager manager) {
        FireGenVariables vars = manager.getFireGenVariables();
        JsonUtilities.stream(vars.municipality(), vars.municipalityFile(), (e) -> {
            JsonObject main = e.getAsJsonObject();
            JsonObject state = JsonUtilities.element(main, "state", false).getAsJsonObject();

            manager.municipality = new SystemMunicipalityImpl(
                    asStr(main, "municipality"),
                    asStr(main, "short"),
                    asStr(main, "dispatch_name"),
                    new SystemMunicipalityImpl.StateImpl(
                            asStr(state, "name"),
                            asStr(state, "abbreviation")
                    )
            );

            Log.info("Imported municipality " + manager.municipality);
        });
    }

    void importAssignmentStatuses(IncidentManager manager) {
        FireGenVariables vars = manager.getFireGenVariables();
        JsonUtilities.stream(vars.municipality(), vars.assignmentStatusFile(), (e) -> {
            JsonArray array = e.getAsJsonArray();

            manager.assignmentStatuses.addAll(List.of(AssignmentStatusImpl.REMOVE_UNIT, AssignmentStatusImpl.ADD_UNIT));
            for (int i = 0; i < array.asList().size(); i++) {
                JsonElement element = array.asList().get(i);
                JsonObject object = element.getAsJsonObject();

                String name = asStr(object, "name");
                String shortName = asStr(object, "short");
                String emojiStr = asStr(object, "emoji");
                Emoji emoji = Emoji.fromFormatted(emojiStr);
                String ansiStr = asStr(object, "ansi");
                AnsiColor ansi = AnsiColor.valueOf(ansiStr.toUpperCase());
                JsonElement purposeElement = JsonUtilities.element(object, "purpose", true);
                AssignmentPurpose purpose = purposeElement != null ? AssignmentPurpose.valueOf(purposeElement.getAsString()) : null;

                AssignmentStatusImpl status = new AssignmentStatusImpl(name, shortName, emoji, new AnsiColor[]{ansi}, i, purpose);
                manager.assignmentStatuses.add(status);
            }

            Log.info("Imported assignment statuses " + String.join(", ", manager.assignmentStatuses));
        });
    }

    void importIncidentStatuses(IncidentManager manager) {
        FireGenVariables vars = manager.getFireGenVariables();
        JsonUtilities.stream(null, vars.incidentStatusFile(), (e) -> {
            JsonArray array = e.getAsJsonArray();
            for (JsonElement element : array.asList()) {
                JsonObject object = element.getAsJsonObject();

                String name = asStr(object, "name");
                String leftEmoji = asStr(object, "emojiLeft");
                String rightEmoji = asStr(object, "emojiRight");

                manager.incidentStatuses.add(new IncidentStatusEmoji(name, leftEmoji, rightEmoji));
            }

            Log.info("Imported incident statuses " + String.join(", ", manager.incidentStatuses));
        });
    }

    void importLocationPresets(IncidentManager manager) {
        FireGenVariables vars = manager.getFireGenVariables();
        JsonUtilities.stream(vars.municipality(), vars.locationPresetsFile(), (e) -> {
            JsonObject root = e.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                String key = entry.getKey();
                JsonObject value = entry.getValue().getAsJsonObject();

                LocationPreset preset = new LocationPreset(manager, key, value);
                manager.presetLocations.add(preset);
            }

            Log.info("Imported " + manager.getPresetLocations().size() + " preset locations.");
        });
    }

    void importRadioChannels(IncidentManager manager) {
        FireGenVariables vars = manager.getFireGenVariables();
        JsonUtilities.stream(vars.municipality(), vars.radioChannelsFile(), (e) -> {
            JsonArray array = e.getAsJsonArray();
            for (JsonElement element : array.asList()) {
                JsonObject object = element.getAsJsonObject();

                String name = asStr(object, "name");
                String alphaTag = asStr(object, "alpha_tag");
                int talkgroupId = asInt(object, "talkgroup_id");

                RadioChannel channel = new RadioChannelImpl(name, alphaTag, talkgroupId);

                manager.radioChannels.add(channel);
            }

            Log.info("Imported " + manager.radioChannels.size() + " radio channels.");
        });
    }

}

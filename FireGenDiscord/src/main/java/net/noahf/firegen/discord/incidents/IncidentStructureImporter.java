package net.noahf.firegen.discord.incidents;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.noahf.firegen.api.incidents.types.IncidentType;
import net.noahf.firegen.api.incidents.types.IncidentTypeTag;
import net.noahf.firegen.api.incidents.status.IncidentStatus;
import net.noahf.firegen.api.incidents.status.IncidentStatusAttributes;
import net.noahf.firegen.api.incidents.status.StatusAttribute;
import net.noahf.firegen.api.incidents.units.AgencyType;
import net.noahf.firegen.api.incidents.units.RadioChannel;
import net.noahf.firegen.api.utilities.FireGenVariables;
import net.noahf.firegen.discord.incidents.structure.*;
import net.noahf.firegen.discord.incidents.structure.location.LocationPreset;
import net.noahf.firegen.discord.incidents.structure.location.LocationVenueImpl;
import net.noahf.firegen.discord.incidents.structure.types.IncidentTypeImpl;
import net.noahf.firegen.discord.incidents.structure.types.IncidentTypeTagImpl;
import net.noahf.firegen.discord.incidents.structure.units.AssignmentStatusImpl;
import net.noahf.firegen.discord.incidents.structure.units.RadioChannelImpl;
import net.noahf.firegen.discord.incidents.structure.units.UnitImpl;
import net.noahf.firegen.discord.utilities.Log;
import net.noahf.firegen.discord.utilities.ansi.AnsiColor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IncidentStructureImporter {

    void importIncidentTypes(IncidentManager manager) {
        FireGenVariables vars = manager.getFireGenVariables();
        String file = vars.municipality() + "/" + vars.incidentTypesFile();
        try
                (InputStream input = this.getClass().getClassLoader().getResourceAsStream(file))
        {
            if (input == null) {
                throw new IllegalStateException("Expected file '" + file + "' to exist, found none.");
            }
            JsonObject object = JsonParser.parseReader(new InputStreamReader(input)).getAsJsonObject();
            List<IncidentTypeTagImpl> tags = new ArrayList<>();
            IncidentTypeTagImpl newTag = null;
            for (JsonElement element : object.getAsJsonArray("tags").asList()) {
                IncidentTypeTagImpl tag = new IncidentTypeTagImpl(element.getAsJsonObject());
                tags.add(tag);
                if (tag.tagName.equalsIgnoreCase("NEW_INCIDENT")) {
                    newTag = tag;
                }
            }
            if (newTag == null) {
                throw new IllegalStateException("Expected an incident tag with name 'NEW_INCIDENT', found none.");
            }

            for (JsonElement element : object.getAsJsonArray("types").asList()) {
                JsonObject obj = element.getAsJsonObject();
                String name = obj.get("name").getAsString();
                String tagStr = obj.get("tag").getAsString();
                IncidentTypeTag tag = tags.stream().filter(itt -> itt.tagName.equalsIgnoreCase(tagStr)).findFirst().orElse(null);
                if (tag == null) {
                    throw new IllegalStateException("Expected type '" + name + "' to have an associated 'tag'");
                }
                List<IncidentType> types = new ArrayList<>();
                if (tagStr.equalsIgnoreCase("NEW_INCIDENT")) {
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
                throw new IllegalStateException("Expected an incident type to be tagged 'NEW_INCIDENT', found none.");
            }

            Log.info("Imported " + manager.incidentTypes.size() + " incident types.");
        } catch (IOException exception) {
            throw new IllegalStateException("IOException: " + exception, exception);
        }
    }

    void importUnits(IncidentManager manager) {
        FireGenVariables vars = manager.getFireGenVariables();
        String file = vars.municipality() + "/" + vars.unitsFile();
        try
                (InputStream input = this.getClass().getClassLoader().getResourceAsStream(file))
        {
            if (input == null) {
                throw new IllegalStateException("Expected file '" + file + "' to exist, found none.");
            }

            JsonArray array = JsonParser.parseReader(new InputStreamReader(input)).getAsJsonArray();
            List<JsonElement> elements = array.asList();
            for (int i = 0; i < elements.size(); i++) {
                JsonObject object = elements.get(i).getAsJsonObject();
                String shorthand = object.get("short").getAsString();
                String longhand = object.get("long").getAsString();
                String format = object.get("format").getAsString();

                JsonElement element = object.get("emoji");
                String emojiStr = element.isJsonNull() ? null : element.getAsString();
                Emoji emoji = emojiStr != null ? Emoji.fromFormatted(emojiStr) : null;

                manager.units.add(new UnitImpl(
                        0L,
                        shorthand, longhand, format, emoji, AgencyType.OTHER,
                        i,
                        SelectOption.of(longhand, shorthand)
                                .withDescription(null)
                                .withEmoji(emoji)
//                                .withEmoji(Emoji.fromFormatted(emoji))
//                                .withEmoji(Emoji.fromCustom(emoji, 0, false))
                ));
            }

            Log.info("Imported units " + String.join(", ", manager.units));
        } catch (IOException exception) {
            throw new IllegalStateException("IOException: " + exception, exception);
        }
    }

    void importVenues(IncidentManager manager) {
        FireGenVariables vars = manager.getFireGenVariables();
        String file = vars.municipality() + "/" + vars.venuesFile();
        try
                (InputStream input = this.getClass().getClassLoader().getResourceAsStream(file))
        {
            if (input == null) {
                throw new IllegalStateException("Expected file '" + file + "' to exist, found none.");
            }

            JsonArray array = JsonParser.parseReader(new InputStreamReader(input)).getAsJsonArray();
            for (JsonElement element : array.asList()) {
                JsonObject object = element.getAsJsonObject();
                String name = object.get("name").getAsString();
                String display = object.get("display").getAsString();

                manager.venues.add(new LocationVenueImpl(name, display));
            }

            vars.setVenues(manager.venues);
            Log.info("Imported venues " + String.join(", ", manager.venues));
        } catch (IOException exception) {
            throw new IllegalStateException("IOException: " + exception, exception);
        }
    }

    void importMunicipality(IncidentManager manager) {
        FireGenVariables vars = manager.getFireGenVariables();
        String file = vars.municipality() + "/" + vars.municipalityFile();
        try
                (InputStream input = this.getClass().getClassLoader().getResourceAsStream(file))
        {
            if (input == null) {
                throw new IllegalStateException("Expected file '" + file + "' to exist, found none.");
            }

            JsonObject main = JsonParser.parseReader(new InputStreamReader(input)).getAsJsonObject();
            JsonObject state = main.getAsJsonObject("state");

            manager.municipality = new SystemMunicipalityImpl(
                    main.get("municipality").getAsString(),
                    main.get("short").getAsString(),
                    main.get("dispatch_name").getAsString(),
                    new SystemMunicipalityImpl.StateImpl(
                            state.get("name").getAsString(),
                            state.get("abbreviation").getAsString()
                    )
            );

            Log.info("Imported municipality " + manager.municipality);
        } catch (IOException exception) {
            throw new IllegalStateException("IOException: " + exception, exception);
        }
    }

    void importAssignmentStatuses(IncidentManager manager) {
        FireGenVariables vars = manager.getFireGenVariables();
        String file = vars.assignmentStatusFile();
        try
                (InputStream input = this.getClass().getClassLoader().getResourceAsStream(file))
        {
            if (input == null) {
                throw new IllegalStateException("Expected file '" + file + "' to exist, found none.");
            }

            JsonArray array = JsonParser.parseReader(new InputStreamReader(input)).getAsJsonArray();

            manager.assignmentStatuses.addAll(List.of(AssignmentStatusImpl.REMOVE_UNIT, AssignmentStatusImpl.HIDE_STATUS));
            for (int i = 0; i < array.asList().size(); i++) {
                JsonElement element = array.asList().get(i);
                JsonObject object = element.getAsJsonObject();

                String name = object.get("name").getAsString();
                String shortName = object.get("short").getAsString();
                String emojiStr = object.get("emoji").getAsString();
                Emoji emoji = Emoji.fromFormatted(emojiStr);
                String ansiStr = object.get("ansi").getAsString();
                AnsiColor ansi = AnsiColor.valueOf(ansiStr.toUpperCase());

                AssignmentStatusImpl status = new AssignmentStatusImpl(name, shortName, emoji, new AnsiColor[]{ansi}, i);
                manager.assignmentStatuses.add(status);
            }

            Log.info("Imported assignment statuses " + String.join(", ", manager.assignmentStatuses));
        } catch (IOException exception) {
            throw new IllegalStateException("IOException: " + exception, exception);
        }
    }

    void importIncidentStatuses(IncidentManager manager) {
        FireGenVariables vars = manager.getFireGenVariables();
        String file = vars.incidentStatusFile();
        try
                (InputStream input = this.getClass().getClassLoader().getResourceAsStream(file))
        {
            if (input == null) {
                throw new IllegalStateException("Expected file '" + file + "' to exist, found none.");
            }

            JsonArray array = JsonParser.parseReader(new InputStreamReader(input)).getAsJsonArray();

            for (JsonElement element : array.asList()) {
                JsonObject object = element.getAsJsonObject();

                String name = object.get("name").getAsString();
                String shortName = object.get("short").getAsString();
                String leftEmoji = object.get("emojiLeft").getAsString();
                String rightEmoji = object.get("emojiRight").getAsString();
                List<StatusAttribute> attributesList = object.get("attributes").getAsJsonArray()
                        .asList().stream()
                        .map(JsonElement::getAsString)
                        .map(StatusAttribute::valueOf)
                        .toList();

                IncidentStatusAttributes attributes = new IncidentStatusAttributes(attributesList);
                IncidentStatus status = new IncidentStatusImpl(
                        name, shortName, leftEmoji, rightEmoji, attributes
                );

                manager.incidentStatuses.add(status);
            }

            Log.info("Imported incident statuses " + String.join(", ", manager.incidentStatuses));
        } catch (IOException exception) {
            throw new IllegalStateException("IOException: " + exception, exception);
        }
    }

    void importLocationPresets(IncidentManager manager) {
        FireGenVariables vars = manager.getFireGenVariables();
        String file = vars.municipality() + "/" + vars.locationPresetsFile();
        try
                (InputStream input = this.getClass().getClassLoader().getResourceAsStream(file))
        {
            if (input == null) {
                throw new IllegalStateException("Expected file '" + file + "' to exist, found none.");
            }

            JsonObject root = JsonParser.parseReader(new InputStreamReader(input)).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                String key = entry.getKey();
                JsonObject value = entry.getValue().getAsJsonObject();

                LocationPreset preset = new LocationPreset(manager, key, value);
                manager.presetLocations.add(preset);
            }

            Log.info("Imported " + manager.getPresetLocations().size() + " preset locations.");
        } catch (IOException exception) {
            throw new IllegalStateException("IOException: " + exception, exception);
        }
    }

    void importRadioChannels(IncidentManager manager) {
        FireGenVariables vars = manager.getFireGenVariables();
        String file = vars.municipality() + "/" + vars.radioChannelsFile();
        try
                (InputStream input = this.getClass().getClassLoader().getResourceAsStream(file))
        {
            if (input == null) {
                throw new IllegalStateException("Expected file '" + file + "' to exist, found none.");
            }

            JsonArray array = JsonParser.parseReader(new InputStreamReader(input)).getAsJsonArray();
            for (JsonElement element : array.asList()) {
                JsonObject object = element.getAsJsonObject();

                String name = object.get("name").getAsString();
                String alphaTag = object.get("alpha_tag").getAsString();
                int talkgroupId = object.get("talkgroup_id").getAsInt();

                RadioChannel channel = new RadioChannelImpl(name, alphaTag, talkgroupId);

                manager.radioChannels.add(channel);
            }

            Log.info("Imported radio channels " + String.join(", ", manager.radioChannels));
        } catch (IOException exception) {
            throw new IllegalStateException("IOException: " + exception, exception);
        }
    }

}

package net.noahf.firegen.discord.incidents;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.managers.CustomEmojiManager;
import net.dv8tion.jda.internal.entities.emoji.CustomEmojiImpl;
import net.noahf.firegen.api.incidents.IncidentType;
import net.noahf.firegen.api.incidents.IncidentTypeTag;
import net.noahf.firegen.api.incidents.units.AgencyType;
import net.noahf.firegen.api.utilities.FireGenVariables;
import net.noahf.firegen.discord.incidents.structure.AgencyImpl;
import net.noahf.firegen.discord.incidents.structure.IncidentTypeImpl;
import net.noahf.firegen.discord.incidents.structure.IncidentTypeTagImpl;
import net.noahf.firegen.discord.incidents.structure.location.LocationVenueImpl;
import net.noahf.firegen.discord.utilities.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

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
                } else if (tag.getQualifier() == null) {
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

    void importAgencies(IncidentManager manager) {
        FireGenVariables vars = manager.getFireGenVariables();
        String file = vars.municipality() + "/" + vars.agenciesFile();
        try
                (InputStream input = this.getClass().getClassLoader().getResourceAsStream(file))
        {
            if (input == null) {
                throw new IllegalStateException("Expected file '" + file + "' to exist, found none.");
            }
            JsonArray array = JsonParser.parseReader(new InputStreamReader(input)).getAsJsonArray();
            for (JsonElement element : array.asList()) {
                JsonObject object = element.getAsJsonObject();
                String shorthand = object.get("short").getAsString();
                String longhand = object.get("long").getAsString();
                String format = object.get("format").getAsString();
                String emoji = object.get("emoji").getAsString();

                manager.agencies.add(new AgencyImpl(
                        shorthand, longhand, format, emoji, AgencyType.OTHER,
                        new ArrayList<>(),
                        SelectOption.of(format, shorthand)
                                .withDescription("(" + shorthand + ") " + longhand)
//                                .withEmoji(Emoji.fromFormatted(emoji))
//                                .withEmoji(Emoji.fromCustom(emoji, 0, false))
                ));
            }

            Log.info("Imported agencies " + String.join(", ", manager.agencies));
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

}

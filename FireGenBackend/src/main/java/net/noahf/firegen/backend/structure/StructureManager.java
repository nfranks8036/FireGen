package net.noahf.firegen.backend.structure;

import com.google.gson.*;
import lombok.Getter;
import net.noahf.firegen.backend.database.structure.Agency;
import net.noahf.firegen.backend.structure.objects.*;
import net.noahf.firegen.backend.utils.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class StructureManager {

    private final ClassLoader loader;
    private final Gson gson;

    private @Getter String municipality;
    private @Getter StructureList<UnitAssignmentStatus> unitAssignmentStatuses;
    private @Getter StructureList<UnitOperationStatus> unitOperationStatuses;
    private @Getter StructureList<RadioChannel> radioChannels;
    private @Getter StructureList<Venue> venues;

    private @Getter StructureList<UnitType> unitTypes;

    private @Getter StructureList<Agency> agencies;

    private @Getter StructureList<IncidentTypeTag> tagsPresets;
    private @Getter StructureList<IncidentType> incidentTypes;

    public StructureManager(String folder) {
        this.loader = StructureManager.class.getClassLoader();
        this.gson = new Gson();

        this.readStructure(folder + "/structure.json");
        this.readUnitTypes(folder + "/unit_types.json");
        this.readAgencies(folder + "/agencies.json");
        this.readIncidentTypes(folder + "/incident_types.json");

        System.out.println(this.incidentTypes.toString());
    }

    private void readStructure(String fileName) {
        try {
            JsonObject json = this.open(fileName);
            Log.debug("Inspecting: " + json.toString());

            this.municipality = json.get("municipality").getAsString();
            this.unitAssignmentStatuses = toStructureList(
                    json.getAsJsonArray("unit_assignment_statuses"),
                    (element) -> {
                        Log.debug("Inspecting: " + element.toString());
                        return new UnitAssignmentStatus(element.getAsString());
                    }
            );
            this.unitOperationStatuses = toStructureList(UnitOperationStatus::new, json.getAsJsonArray("unit_operation_statuses"));
            this.radioChannels = toStructureList(RadioChannel::new, json.getAsJsonArray("radio_channels"));
            this.venues = toStructureList(Venue::new, json.getAsJsonArray("venues"));
        } catch (Exception exception) {
            throw new RuntimeException("Failed to read '" + fileName + "': " + exception, exception);
        }
    }
    private void readUnitTypes(String fileName) {
        try {
            JsonObject json = this.open(fileName);

            this.unitTypes = toStructureList(
                    json.getAsJsonArray("types"),
                    (element) -> {
                        JsonObject obj = element.getAsJsonObject();
                        Log.debug("Inspecting: " + obj.toString());
                        return new UnitType(obj.get("name"), obj.get("callsign"), obj.get("abbreviation"));
                    });
        } catch (Exception exception) {
            throw new RuntimeException("Failed to read '" + fileName + "': " + exception, exception);
        }
    }
    private void readAgencies(String fileName) {
        try {
            JsonObject json = this.open(fileName);
            Log.debug("Inspecting: " + json.toString());

            this.agencies = toStructureList(
                    json.getAsJsonArray("agencies"),
                    (element) -> {
                        JsonObject obj = element.getAsJsonObject();
                        return new Agency(this, obj);
                    });
        } catch (Exception exception) {
            throw new RuntimeException("Failed to read '" + fileName + "': " + exception, exception);
        }
    }
    private void readIncidentTypes(String fileName) {
        try {
            JsonObject json = this.open(fileName);

            this.tagsPresets = toStructureList(
                    json.getAsJsonArray("tags"),
                    (element) -> {
                        JsonObject obj = element.getAsJsonObject();
                        Log.debug("Inspecting: " + obj.toString());

                        JsonElement qualifierElement = obj.get("qualifiers");
                        IncidentTypeTag.Qualifier qualifier = null;
                        if (!qualifierElement.isJsonNull()) {
                            JsonObject qual = qualifierElement.getAsJsonObject();
                            qualifier = new IncidentTypeTag.Qualifier(
                                    qual.get("required").getAsBoolean(),
                                    qual.get("unique").getAsBoolean(),
                                    qual.get("syntax").getAsString(),
                                    qual.getAsJsonArray("list").asList().stream().map(JsonElement::getAsString).toList()
                            );
                        }

                        return new IncidentTypeTag(
                                obj.get("name").getAsString(),
                                obj.getAsJsonArray("priorities").asList().stream().map(JsonElement::getAsString).toList(),
                                qualifier
                        );
                    });

            this.incidentTypes = toStructureList(
                    json.getAsJsonArray("types"),
                    (element) -> {
                        JsonObject obj = element.getAsJsonObject();
                        Log.debug("Inspecting: " + obj.toString());
                        return new IncidentType(this, obj);
                    });
        } catch (Exception exception) {
            throw new RuntimeException("Failed to read '" + fileName + "': " + exception, exception);
        }
    }









    private JsonObject open(String fileName) throws IOException {
        try
                (InputStream input = loader.getResourceAsStream(fileName))
        {
            if (input == null) {
                throw new IllegalStateException("Expected a file namde '" + fileName + "', found none.");
            }
            return JsonParser.parseReader(
                    new InputStreamReader(input)
            ).getAsJsonObject();
        }
    }

    private <T extends StructureObject> StructureList<T> toStructureList(Function<String, T> generator, JsonArray array) {
        return this.toStructureList(array, (element) -> generator.apply(element.getAsString()));
    }

    private <T extends StructureObject> StructureList<T> toStructureList(JsonArray array, Function<JsonElement, T> generator) {
        List<T> objs = new ArrayList<>();
        for (JsonElement obj : array.asList()) {
            objs.add(generator.apply(obj));
        }

        return new StructureList<>(objs);
    }

    @Override
    public String toString() {
        return "StructureManager{" +
                "municipality='" + municipality + '\'' +
                ", unitAssignmentStatuses=" + unitAssignmentStatuses +
                ", unitOperationStatuses=" + unitOperationStatuses +
                ", radioChannels=" + radioChannels +
                ", unitTypes=" + unitTypes +
                ", agencies=" + agencies +
                ", incidentTypes=" + incidentTypes +
                '}';
    }
}

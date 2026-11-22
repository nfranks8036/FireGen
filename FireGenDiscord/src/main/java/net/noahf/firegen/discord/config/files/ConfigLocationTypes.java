package net.noahf.firegen.discord.config.files;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.noahf.firegen.api.FireGenVariables;
import net.noahf.firegen.api.incidents.location.LocationField;
import net.noahf.firegen.api.incidents.location.LocationType;
import net.noahf.firegen.discord.config.MultiObjectConfiguration;
import net.noahf.firegen.discord.incidents.structure.location.LocationTypeImpl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static net.noahf.firegen.discord.utilities.JsonUtilities.asStr;
import static net.noahf.firegen.discord.utilities.JsonUtilities.element;

public class ConfigLocationTypes extends MultiObjectConfiguration<LocationType> {

    private LocationType fallback = null;
    private @Getter final List<SelectOption> selectOptions = new ArrayList<>();

    public ConfigLocationTypes(FireGenVariables vars) {
        super(vars, LocationType.class, "location_types.json");
    }

    @Override
    public void importObject(JsonElement e) {
        JsonArray array = e.getAsJsonArray();

        for (JsonElement element : array.asList()) {
            JsonObject obj = element.getAsJsonObject();

            String id = asStr(obj, "id");
            String title = asStr(obj, "title");
            String shortName = asStr(obj, "short");
            String description = asStr(obj, "description");
            String defaultDataDelimiter = asStr(obj, "defaultDataDelimiter");
            List<JsonElement> fieldsArray = element(obj, "fields", false, (f) -> f.getAsJsonArray().asList());
            boolean fallback = Boolean.TRUE.equals(element(obj, "fallback", true, JsonElement::getAsBoolean));

            List<LocationField> fields = new LinkedList<>();
            for (JsonElement field : Objects.requireNonNull(fieldsArray)) {
                obj = field.getAsJsonObject();

                if (obj.get("use") != null) {
                    fields.addAll(findFields(obj));
                    continue;
                }

                String fieldId = asStr(obj, "id");
                String fieldTitle = asStr(obj, "title");
                String fieldDesc = asStr(obj, "description");
                Boolean required = element(obj, "required", false, JsonElement::getAsBoolean);
                Integer minLength = element(obj, "minLength", false, JsonElement::getAsInt);
                Integer maxLength = element(obj, "maxLength", false, JsonElement::getAsInt);
                String placeholder = asStr(obj, "placeholder");
                Integer autofillDataIndex = element(obj, "autofillDataIndex", false, JsonElement::getAsInt);

                LocationField.LocationFieldBuilder builder = LocationField.newField(fieldTitle, fieldDesc, fieldId)
                        .setRequired(Boolean.TRUE.equals(required))
                        .setPlaceholder(placeholder);

                if (minLength != null) builder = builder.setMinLength(minLength);
                if (maxLength != null) builder = builder.setMaxLength(maxLength);
                if (autofillDataIndex != null) builder = builder.setAutofillIndex(autofillDataIndex);

                fields.add(builder.build());
            }

            LocationType type = new LocationTypeImpl(
                    "Location", id, title, shortName, description, defaultDataDelimiter, fields.toArray(LocationField[]::new)
            );
            this.add(type);

            selectOptions.add(
                    SelectOption.of(type.getTitle(), type.getId())
                            .withDescription(type.getDescription())
            );

            if (fallback && type.getFields().length == 1) {
                this.fallback = type;
            }
        }

        if (fallback == null) {
            throw new IllegalStateException(
                    "Need at least one Location Type where 'fallback' is true in which only one field exists: " +
                            "a text box. This Location Type is used as fallback if the data cannot be interpreted."
            );
        }

        log("Imported location types " + this.get().stream().map(LocationType::getTitle).collect(Collectors.joining(", ")));
    }

    public LocationType fromId(String id) {
        for (LocationType type : this.get()) {
            if (type.getId().equalsIgnoreCase(id)) {
                return type;
            }
        }
        return null;
    }

    private List<LocationField> findFields(JsonObject obj) {
        List<LocationField> fields = new LinkedList<>();
        for (JsonElement item : Objects.requireNonNull(element(obj, "use", false, (e) -> e.getAsJsonArray().asList()))) {
            String s = item.getAsString();
            if (s.equalsIgnoreCase("COMMON_NAME")) {
                fields.add(LocationField.COMMON_NAME);
            } else if (s.equalsIgnoreCase("VENUE")) {
                fields.add(LocationField.VENUE);
            }
        }
        return fields;
    }

}

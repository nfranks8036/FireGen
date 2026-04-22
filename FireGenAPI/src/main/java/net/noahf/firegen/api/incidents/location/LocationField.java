package net.noahf.firegen.api.incidents.location;

import lombok.Builder;
import lombok.Getter;

@Builder(builderMethodName = "", setterPrefix = "set")
@Getter
public class LocationField {

    public static LocationFieldBuilder newField(String title, String description, String id, TextInputStyle type) {
        return new LocationFieldBuilder()
                // inputted required values
                .setTitle(title)
                .setDescription(description)
                .setId(id)
                .setType(type)

                // default values
                .setRequired(true)
                .setMinLength(-1)
                .setMaxLength(-1)
                .setPlaceholder(null);
    }

    public static final LocationField VENUE = LocationField.newField(
            "Venue",
            "OPTIONAL: {VENUES}",
            "venue",
            TextInputStyle.SHORT
    )
            .setRequired(false)
            .build();

    public static final LocationField COMMON_NAME = LocationField.newField(
            "Common Name",
            "OPTIONAL: The name the general public refers to this location as.",
            "common-name",
            TextInputStyle.SHORT
    )
            .setRequired(false)
            .setMaxLength(100)
            .setPlaceholder("Ex: Municipal Building")
            .build();




    private final String title;
    private final String description;
    private final String id;
    private final TextInputStyle type;

    private boolean required = true;
    private int minLength = -1;
    private int maxLength = -1;
    private String placeholder = null;


    public enum TextInputStyle {
        SHORT, PARAGRAPH
    }

}

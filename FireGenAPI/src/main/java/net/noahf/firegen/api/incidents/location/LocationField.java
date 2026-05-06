package net.noahf.firegen.api.incidents.location;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.stream.Collectors;

@Builder(builderMethodName = "", setterPrefix = "set", toBuilder = true)
@Getter
@EqualsAndHashCode
@ToString
public class LocationField {

    public static LocationFieldBuilder newField(String title, String description, String id, TextType type) {
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

    public static LocationField VENUE = LocationField.newField(
            "Venue",
            "OPTIONAL: {VENUES}",
            "venue",
            TextType.SHORT
    )
            .setRequired(false)
            .build();

    public static final LocationField COMMON_NAME = LocationField.newField(
            "Common Name",
            "OPTIONAL: The name the general public refers to this location as.",
            "common-name",
            TextType.SHORT
    )
            .setRequired(false)
            .setMaxLength(100)
            .setPlaceholder("Ex: Municipal Building")
            .build();




    private final String title;
    private final String description;
    private final String id;
    private final TextType type;

    private boolean required = true;
    private int minLength = -1;
    private int maxLength = -1;
    private String placeholder = null;

    public enum TextType {
        SHORT, PARAGRAPH
    }



    private static boolean setFields = false;

    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    public static void setKnownVenues(List<LocationVenue> venues) {
        if (setFields) {
            throw new IllegalStateException("Already patched the venue descriptions, setKnownVenues has been disabled.");
        }

        for (LocationType value : LocationType.values()) {
            LocationField.VENUE = LocationField.VENUE.toBuilder()
                    .setDescription(VENUE.getDescription().replace(
                            "{VENUES}",
                            venues.stream().map(LocationVenue::getName)
                                    .collect(Collectors.joining(", "))
                    ))
                    .build();
            value.patchVenue(LocationField.VENUE);
        }

        setFields = true;
    }

}

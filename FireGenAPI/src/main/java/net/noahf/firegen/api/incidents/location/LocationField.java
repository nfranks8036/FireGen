package net.noahf.firegen.api.incidents.location;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.noahf.firegen.api.incidents.Incident;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents a desired {@link IncidentLocation} input field, which is useful for determining what the user UI for the
 * input of an {@link Incident incident's} location should consist of.
 */
@Builder(builderMethodName = "", setterPrefix = "set", toBuilder = true)
@Getter
@EqualsAndHashCode
@ToString
public class LocationField {

    /**
     * Requires a specific venue type before this function will be utilized. This is useful for auto-filling UI fields
     * so that the UI will not interpret data liberally.
     * @param function the parent function
     * @param type the required type (the enum name of {@link LocationType})
     * @return the function with the added requirement of the {@link LocationType}=={@code type}
     */
    public static Function<Incident, String> requireType(Function<IncidentLocation, String> function, String type) {
        return (i) -> {
            try {
                IncidentLocation location = i.getLocation();
                if (!location.getType().name().equalsIgnoreCase(type)) {
                    return null;
                }
                return function.apply(location);
            } catch (Exception exception) {
                return null;
            }
        };
    }

    /**
     * Creates a new {@link LocationField} based on some presets.
     * @param title see {@link LocationField#getTitle()}
     * @param description see {@link LocationField#getDescription()}
     * @param id see {@link LocationField#getId()}
     * @param type see {@link LocationField#getType()}
     */
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

    /**
     * See {@link IncidentLocation#getVenue()} for more information.
     */
    public static LocationField VENUE = LocationField.newField(
            "Venue",
            "OPTIONAL: {VENUES}",
            "venue",
            TextType.SHORT
    )
            .setRequired(false)
            .setAutofill((i) -> {
                IncidentLocation location = i.getLocation();
                if (location.getVenue() == null) return null;
                return location.getVenue().getName();
            })
            .build();

    /**
     * See {@link IncidentLocation#getCommonName()} for more information.
     */
    public static final LocationField COMMON_NAME = LocationField.newField(
            "Common Name",
            "OPTIONAL: The name the general public refers to this location as.",
            "common-name",
            TextType.SHORT
    )
            .setRequired(false)
            .setMaxLength(100)
            .setPlaceholder("Ex: Municipal Building")
            .setAutofill((i) -> i.getLocation().getCommonName())
            .build();

    /**
     * Represents the title for the field, such as "Street Name"
     */
    private final String title;

    /**
     * Represents the description of the field, such as "The name of the street the numeric address is on."
     */
    private final String description;

    /**
     * Represents the ID that uniquely identifies this field, such as "address-numerics". This ID typically includes
     * the LocationType in it before the "-" (in this case, "address" is the location type).
     */
    private final String id;

    /**
     * Represents the input field type as a {@link TextType}, view that class for more information.
     */
    private final TextType type;

    /**
     * Represents if the field is required for the {@link LocationType} to function.
     */
    private boolean required = true;

    /**
     * Represents the minimum text length the LocationType expects of the field.
     */
    private int minLength = -1;

    /**
     * Represents the maximum text length the LocationType expects of the field.
     */
    private int maxLength = -1;

    /**
     * Represents the text that is the 'placeholder', which is usally the grayed-out text that the end-user can
     * write over.
     */
    private String placeholder = null;

    /**
     * Represents what text would already be in the field, typically a value already set in the {@link Incident}
     */
    private Function<Incident, String> autofill = null;


    /**
     * The TextType represents how the text box is formatted for the end-user. Noteably, this has <b>no effect</b>
     * on the minimum or maximum text length, that detail is set in the {@link LocationField#getMaxLength()} and
     * {@link LocationField#getMinLength()}.
     */
    public enum TextType {
        SHORT, PARAGRAPH
    }



    private static boolean setFields = false;

    /**
     * <b>Not for API use.</b>
     * @deprecated internal method only, do not use, you will almost 100% of the time get a Void return with some text
     * printed in {@link System#err}.
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    public static void setKnownVenues(List<LocationVenue> venues) {
        if (setFields) {
            System.err.println("Already patched the venue descriptions, setKnownVenues has been disabled!");
            return;
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

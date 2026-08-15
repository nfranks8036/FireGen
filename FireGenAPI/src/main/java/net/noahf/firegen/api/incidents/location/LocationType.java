package net.noahf.firegen.api.incidents.location;

import lombok.AccessLevel;
import lombok.Getter;
import net.noahf.firegen.api.utilities.IgnoreStringSelector;
import net.noahf.firegen.api.utilities.StringSelectors;

import java.util.ArrayList;
import java.util.List;

import static net.noahf.firegen.api.incidents.location.LocationField.*;

/**
 * Represents the type of Location inputs that are available to be saved inside an {@link net.noahf.firegen.api.incidents.Incident}.
 * After loading at compile-time, this is entirely customizable by the end-developer.
 */
@Getter
public class LocationType implements StringSelectors {

    private static final List<LocationType> locationTypes = new ArrayList<>();

    /**
     * Represents a traditional address that identifies a home, business, commercial establishment, industrial plant, or
     * other types of locations. The address expects numerics (e.g., 123) and a street name (e.g., Main St). The address
     * accepts a {@link LocationField#COMMON_NAME common name} and a {@link LocationField#VENUE venue}.
     */
    public static LocationType ADDRESS = new LocationType(
            "Location",
            "ADDRESS",
            "Address",
            "A numeric address. Requires: Street address, including numerics. Allows: Common name, venue.",
            " ",
            newField("Address Numerics", "The numbers representing the address.", "address-numerics", TextType.SHORT)
                    .setRequired(true)
                    .setMinLength(2)
                    .setMaxLength(8)
                    .setPlaceholder("Ex: 2800")
                    .setAutofill(requireType((l) -> l.getData().getFirst(), "ADDRESS"))
                    .build(),
            newField("Street Name", "The name of the street the numeric address is on.", "address-street", TextType.SHORT)
                    .setRequired(true)
                    .setMinLength(2)
                    .setMaxLength(100)
                    .setPlaceholder("Ex: Commerce St")
                    .setAutofill(requireType((l) -> l.getData().get(1), "ADDRESS"))
                    .build(),
            COMMON_NAME,
            VENUE
    );

    /**
     * Represents a mile-marker or landmark location that generally identifies a specific point along a road, highway,
     * route, or interstate. The mile-marker or landmark expects a road name (e.g., 'I-81 NB') and a
     * mile-marker/landmark (e.g., 'MM 114' or 'Exit 5' or 'Jeanelle Rd Overpass'). The mile-marker or landmark accepts
     * a {@link LocationField#VENUE}.
     */
    public static final LocationType MILE_MARKER = new LocationType(
            "Location",
            "MILE_MARKER",
            "Mile-Marker / Landmark",
            "A mile-marker or landmark on a road. Requires: Mile marker/landmark, road name. Allows: Venue.",
            " @ ",
            newField("Road Name", "The road the call is on. Use 'US-' for US routes and 'I-' for interstates. Add direction of travel.", "milemarker-roadname", TextType.SHORT)
                    .setRequired(true)
                    .setMinLength(1)
                    .setMaxLength(20)
                    .setPlaceholder("Ex: I-81 NB")
                    .setAutofill(requireType((l) -> l.getData().getFirst(), "MILE_MARKER"))
                    .build(),
            newField("Mile-Marker / Landmark", "The mile-marker or landmark. Add 'MM' before a mile-marker.", "milemarker-landmark", TextType.SHORT)
                    .setRequired(true)
                    .setMinLength(1)
                    .setMaxLength(40)
                    .setPlaceholder("Ex: MM 114 *OR* Exit 5")
                    .setAutofill(requireType((l) -> l.getData().get(1), "MILE_MARKER"))
                    .build(),
            VENUE
    );

    /**
     * Represents a latitude and longitude position that identifies the exact coordinates of where an incident is
     * occurring at. This is useful if there is no other matching fields that make sense. The latitude and longitude
     * expects a latitude (in decimal degrees, e.g., 37.199022) and a
     * longitude (in decimal degrees, e.g., -80.3968628). The latitude and longitude accepts additional information
     * (e.g., "Between the Blacksburg Transit building and Cedar Run Rd in the woods") and a
     * {@link LocationField#VENUE venue}.
     */
    public static final LocationType LATITUDE_LONGITUDE = new LocationType(
            "Location",
            "LATITUDE_LONGITUDE",
            "Latitude & Longitude",
            "A latitude and longitude. Requires: Two float values. Allows: Additional information, venue.",
            ", ",
            newField("Latitude", "The latitude, in *DECIMAL DEGREES*, of the incident.", "latitudelongitude-latitude", TextType.SHORT)
                    .setRequired(true)
                    .setMinLength(1)
                    .setMaxLength(10)
                    .setPlaceholder("Ex: 37.197523")
                    .setAutofill(requireType((l) -> l.getData().getFirst(), "LATITUDE_LONGITUDE"))
                    .build(),
            newField("Longitude", "The longitude, in *DECIMAL DEGREES*, of the incident.", "latitudelongitude-longitude", TextType.SHORT)
                    .setRequired(true)
                    .setMinLength(1)
                    .setMaxLength(10)
                    .setPlaceholder("Ex: -80.395021")
                    .setAutofill(requireType((l) -> l.getData().get(1), "LATITUDE_LONGITUDE"))
                    .build(),
            newField("Additional Information", "OPTIONAL: More information on what is at this location.", "latitudelongitude-additional", TextType.PARAGRAPH)
                    .setRequired(false)
                    .setPlaceholder("Ex: Parking Lot of Blacksburg Transit")
                    .setAutofill(requireType((l) -> l.getData().get(2), "LATITUDE_LONGITUDE"))
                    .build(),
            VENUE
    );

    /**
     * Represents an intersection between two or more roads. The intersection expects at least two roads (e.g.,
     * "Prices Fork Rd" for Road #1 and "Stanger St" for Road #2 is the intersection of those two roads). The
     * intersection accepts up to four roads (e.g., "Prices Fork Rd", "Stanger St", and "Toms Creek Rd"). The
     * intersection accepts a {@link LocationField#VENUE venue}.
     */
    public static final LocationType INTERSECTION = new LocationType(
            "Location",
            "INTERSECTION",
            "Intersection",
            "An intersection of two roads. Requires: Two or more roads. Allows: Multiple roads.",
            " / ",
            newField("Intersection: Road #1", "The first road in the intersection.", "intersection-road1", TextType.SHORT)
                    .setRequired(true)
                    .setPlaceholder("Ex: N Main St")
                    .setMaxLength(20)
                    .setAutofill(requireType((l) -> l.getData().getFirst(), "INTERSECTION"))
                    .build(),
            newField("Intersection: Road #2", "The second road in the intersection.", "intersection-road2", TextType.SHORT)
                    .setRequired(true)
                    .setPlaceholder("Ex: Progress St")
                    .setMaxLength(20)
                    .setAutofill(requireType((l) -> l.getData().get(1), "INTERSECTION"))
                    .build(),
            newField("Intersection: Road #3", "OPTIONAL: The third road in the intersection.", "intersection-road3", TextType.SHORT)
                    .setRequired(false)
                    .setPlaceholder("Ex: McDonald St")
                    .setMaxLength(20)
                    .setAutofill(requireType((l) -> l.getData().get(2), "INTERSECTION"))
                    .build(),
            VENUE
    );

    /**
     * Represents the cross-streets of a location with one or more roads. This is best for if you do not want to provide
     * an exact location due to privacy concerns but want to allow end-users to see the general location. The
     * cross-streets field expects at least one road but will allow up to three. The cross-streets accepts a
     * {@link LocationField#VENUE venue}.
     */
    public static final LocationType CROSS_STREETS = new LocationType(
            "Cross-streets",
            "CROSS_STREETS",
            "Cross-streets",
            "Two cross-streets for generic locations. Requires: At least one road. Allows: Multiple roads.",
            ", ",
            newField("Cross-street: Road #1", "The primary road in the cross-streets.", "crossstreets-road1", TextType.SHORT)
                    .setRequired(true)
                    .setPlaceholder("Ex: N Main St")
                    .setMaxLength(20)
                    .setAutofill(requireType((l) -> l.getData().getFirst(), "CROSS_STREETS"))
                    .build(),
            newField("Cross-street: Road #2", "OPTIONAL: The secondary road in the cross-streets.", "crossstreets-road2", TextType.SHORT)
                    .setRequired(false)
                    .setPlaceholder("Ex: Progress St")
                    .setMaxLength(20)
                    .setAutofill(requireType((l) -> l.getData().get(1), "CROSS_STREETS"))
                    .build(),
            newField("Cross-street: Road #3", "OPTIONAL: The tertiary road in the cross-streets.", "crossstreets-road3", TextType.SHORT)
                    .setRequired(false)
                    .setPlaceholder("Ex: McDonald St")
                    .setMaxLength(20)
                    .setAutofill(requireType((l) -> l.getData().get(2), "CROSS_STREETS"))
                    .build(),
            VENUE
    );

    /**
     * Represents a custom-formatted text location that is the catch-all for any text that does not fit the above
     * fields. The custom text format accepts a {@link LocationField#VENUE venue}.
     */
    public static final LocationType CUSTOM = new LocationType(
            "Location",
            "CUSTOM",
            "Custom Location",
            "Custom text to describe the location if none of the above fit.",
            ", ",
            newField("Custom Text", "Enter the custom location type in this box.", "custom-custom", TextType.PARAGRAPH)
                    .setRequired(true)
                    .setMaxLength(200)
                    .setAutofill(requireType((l) -> l.getData().getFirst(), "CUSTOM"))
                    .build(),
            VENUE
    );


    static {
        locationTypes.addAll(List.of(
                ADDRESS, MILE_MARKER, LATITUDE_LONGITUDE, INTERSECTION, CROSS_STREETS, CUSTOM
        ));
    }

    /**
     * Retrieve the list of all currently loaded {@link LocationType location types}.
     * @return the currently loaded location types.
     */
    public static List<LocationType> values() {
        return new ArrayList<>(locationTypes);
    }

    /**
     * Clear all the location types. If left in this mode, no locations will be accessible to the end-user.
     * @see List#clear()
     */
    public static void clearLocationTypes() {
        locationTypes.clear();
    }

    /**
     * Insert a location into the list of saved and loaded locations.
     * @param type the {@link LocationType} to save.
     * @see List#add(Object)
     */
    public static boolean insertLocationType(LocationType type) {
        return locationTypes.add(type);
    }

    /**
     * Deletes a location type from the list of saved and loaded locations.
     * @param type the {@link LocationType} to remove.
     * @see List#remove(Object)
     */
    public static boolean deleteLocationType(LocationType type) {
        return locationTypes.remove(type);
    }

    /**
     * Finds a corresponding {@link LocationType} object by searching for its {@link LocationType#getId()}.
     * @param id the string ID to match against
     * @return the {@link LocationType} with that id
     * @throws IllegalArgumentException if the provided {@code id} is not a valid object in the
     *                                  {@link LocationType#values()}
     */
    public static LocationType valueOf(String id) {
        for (LocationType lt : locationTypes) {
            if (lt.id.equalsIgnoreCase(id)) {
                return lt;
            }
        }
        throw new IllegalArgumentException("No LocationType exists by the ID '" + id + "'");
    }


    private final @Getter(value = AccessLevel.NONE) String description;
    private final String prefix, id, title, defaultDataDelimiter;
    private final LocationField[] fields;

    LocationType(String prefix, String id, String title, String description, String defaultDataDelimiter, LocationField... fields) {
        this.prefix = prefix;
        this.id = id;
        this.title = title;
        this.description = description;
        this.defaultDataDelimiter = defaultDataDelimiter;
        this.fields = fields;
    }

    /**
     * @deprecated Not for general use!
     */
    @Deprecated
    void patchVenue(LocationField field) {
        for (int i = 0; i < this.fields.length; i++) {
            if (!this.fields[i].getTitle().equalsIgnoreCase(field.getTitle())) {
                continue;
            }

            this.fields[i] = field;
            break;
        }
    }

    @IgnoreStringSelector
    public String getDescription() {
        return this.description;
    }

    @Override
    public List<String> asStringSelectors() {
        return List.of(this.name());
    }

    /**
     * Retrieve the ID (name) of the current field
     * @return the string name of this field, this is typically what a {@link Enum#name()} field would be.
     */
    @IgnoreStringSelector
    public String name() {
        return this.getId();
    }

}

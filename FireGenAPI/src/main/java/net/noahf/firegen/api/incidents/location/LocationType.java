package net.noahf.firegen.api.incidents.location;

import lombok.Getter;
import lombok.experimental.Accessors;

import static net.noahf.firegen.api.incidents.location.LocationField.*;

public enum LocationType {

    ADDRESS(
            "Address",
            "A numeric address. Requires: Street address, including numerics. Allows: Common name, venue.",
            newField("Address Numerics", "The numbers representing the address.", "address-numerics", TextInputStyle.SHORT)
                    .setRequired(true)
                    .setMinLength(2)
                    .setMaxLength(8)
                    .setPlaceholder("Ex: 2800")
                    .build(),
            newField("Street Name", "The name of the street the numeric address is on.", "address-street", TextInputStyle.SHORT)
                    .setRequired(true)
                    .setMinLength(2)
                    .setMaxLength(100)
                    .setPlaceholder("Ex: Commerce St")
                    .build(),
            COMMON_NAME,
            VENUE
    ),

    MILE_MARKER(
            "Mile-Marker / Landmark",
            "A mile-marker or landmark on a road. Requires: Mile marker/landmark, road name. Allows: Venue.",
            newField("Road Name", "The road the call is on. Use 'US-' for US routes and 'I-' for interstates. Add direction of travel.", "milemarker-roadname", TextInputStyle.SHORT)
                    .setRequired(true)
                    .setMinLength(1)
                    .setMaxLength(20)
                    .setPlaceholder("Ex: I-81 NB").build(),
            newField("Mile-Marker / Landmark", "The mile-marker or landmark. Add 'MM' before a mile-marker.", "milemarker-landmark", TextInputStyle.SHORT)
                    .setRequired(true)
                    .setMinLength(1)
                    .setMaxLength(20)
                    .setPlaceholder("Ex: MM 114 *OR* Exit 5")
                    .build(),
            VENUE
    ),

    LATITUDE_LONGITUDE(
            "Latitude & Longitude",
            "A latitude and longitude. Requires: Two float values. Allows: Additional information, venue.",
            newField("Latitude", "The latitude, in *DECIMAL DEGREES*, of the incident.", "latitudelongitude-latitude", TextInputStyle.SHORT)
                    .setRequired(true)
                    .setMinLength(1)
                    .setMaxLength(10)
                    .setPlaceholder("Ex: 37.197523").build(),
            newField("Longitude", "The longitude, in *DECIMAL DEGREES*, of the incident.", "latitudelongitude-longitude", TextInputStyle.SHORT)
                    .setRequired(true)
                    .setMinLength(1)
                    .setMaxLength(10)
                    .setPlaceholder("Ex: -80.395021").build(),
            newField("Additional Information", "OPTIONAL: More information on what is at this location.", "latitudelongitude-additional", TextInputStyle.PARAGRAPH)
                    .setRequired(false)
                    .setPlaceholder("Ex: Parking Lot of Blacksburg Transit").build(),
            VENUE
    ),

    INTERSECTION(
            "Intersection",
            "An intersection of two roads. Requires: Two or more roads. Allows: Multiple roads.",
            newField("Intersection: Road #1", "The first road in the intersection.", "intersection-road1", TextInputStyle.SHORT)
                    .setRequired(true)
                    .setPlaceholder("Ex: N Main St")
                    .setMaxLength(20)
                    .build(),
            newField("Intersection: Road #2", "The second road in the intersection.", "intersection-road2", TextInputStyle.SHORT)
                    .setRequired(true)
                    .setPlaceholder("Ex: Progress St")
                    .setMaxLength(20)
                    .build(),
            newField("Intersection: Road #3", "OPTIONAL: The third road in the intersection.", "intersection-road3", TextInputStyle.SHORT)
                    .setRequired(false)
                    .setPlaceholder("Ex: McDonald St")
                    .setMaxLength(20)
                    .build(),
            newField("Intersection: Road #4", "OPTIONAL: The fourth road in the intersection.", "intersection-road4", TextInputStyle.SHORT)
                    .setRequired(false)
                    .setPlaceholder("Ex: Winston Ave")
                    .setMaxLength(20)
                    .build()
    ),

    CROSS_STREETS(
            "Cross-streets",
            "Two cross-streets for generic locations. Requires: At least one road. Allows: Multiple roads.",
            newField("Cross-street: Road #1", "The primary road in the cross-streets.", "crossstreets-road1", TextInputStyle.SHORT)
                    .setRequired(true)
                    .setPlaceholder("Ex: N Main St")
                    .setMaxLength(20)
                    .build(),
            newField("Cross-street: Road #2", "OPTIONAL: The secondary road in the cross-streets.", "crossstreets-road2", TextInputStyle.SHORT)
                    .setRequired(false)
                    .setPlaceholder("Ex: Progress St")
                    .setMaxLength(20)
                    .build(),
            newField("Cross-street: Road #3", "OPTIONAL: The tertiary road in the cross-streets.", "crossstreets-road3", TextInputStyle.SHORT)
                    .setRequired(false)
                    .setPlaceholder("Ex: McDonald St")
                    .setMaxLength(20)
                    .build(),
            newField("Cross-street: Road #4", "OPTIONAL: The quaternary road in the cross-streets.", "crossstreets-road4", TextInputStyle.SHORT)
                    .setRequired(false)
                    .setPlaceholder("Ex: Winston Ave")
                    .setMaxLength(20)
                    .build()
    ),

    CUSTOM(
            "Custom Location",
            "Custom text to describe the location if none of the above fit.",
            newField("Custom Text", "Enter the custom location type in this box.", "custom-custom", TextInputStyle.PARAGRAPH)
                    .setRequired(true)
                    .setMaxLength(200)
                    .build()
    );

    private final @Getter @Accessors(fluent = true) String title, description;
    private final @Getter @Accessors(fluent = true) LocationField[] fields;

    LocationType(String title, String description, LocationField... fields) {
        this.title = title;
        this.description = description;
        this.fields = fields;
    }

}

package net.noahf.firegen.discord.incidents.structure.location;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.noahf.firegen.api.incidents.location.IncidentLocation;
import net.noahf.firegen.api.incidents.location.LocationType;
import net.noahf.firegen.api.incidents.location.LocationVenue;
import net.noahf.firegen.api.utilities.AutofilledCharSequence;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.StringJoiner;

/**
 * Represents a location of an {@link IncidentImpl Incident}.
 */
@NoArgsConstructor(force = true)
@Getter @Setter
@Entity
public class IncidentLocationImpl implements IncidentLocation, AutofilledCharSequence {

    /**
     * Creates a location of type {@link LocationType#CUSTOM} with custom data, the data will be shown as written.
     */
    public IncidentLocationImpl(List<String> custom) {
        this(custom, LocationType.CUSTOM, null, null);
    }

    public IncidentLocationImpl(List<String> data, LocationType type, @Nullable String commonName, @Nullable LocationVenue venue) {
        this.data = data;
        this.type = type;
        this.commonName = commonName;
        this.venue = venue;
    }

//    /**
//     * The {@link Label} representing the Common Name, which is the name the public may know the location by.
//     */
//    final Label COMMON_NAME = Label.of("Common Name", "OPTIONAL: The name the general public may know this place by.", TextInput.create("common-name", TextInputStyle.SHORT)
//            .setRequired(false)
//            .setMaxLength(100)
//            .setPlaceholder("Ex: Blacksburg Transit")
//            .build()
//    );
//
//    /**
//     * The {@link Label} representing the {@link LocationVenueImpl}, which is the general location that an Incident occurs
//     */
//    final Label VENUE = Label.of("Venue",
//            "OPTIONAL: ALLOWED: " + Main.incidents.getConcatenatedVenues().substring(0, Math.min(100 - "OPTIONAL: ALLOWED: ".length(), Main.incidents.getConcatenatedVenues().length())),
//            TextInput.create("venue", TextInputStyle.SHORT)
//                    .setRequired(false)
//
//                    // set the placeholder value to the first value, because it should be set
//                    .setPlaceholder(Main.incidents.getVenues().get(0).getName())
//
//                    // the minimum length and maximum length are the maximum and minimum of the venues to encourage
//                    // only selecting a venue that is valid
//                    .setMaxLength(Main.incidents.getVenues().stream()
//                            .map(LocationVenueImpl::getName)
//                            .max(Comparator.comparingInt(String::length))
//                            .stream()
//                            .findFirst()
//                            .orElse(" ".repeat(100))
//                            .length()
//                    )
//                    .setMinLength(Main.incidents.getVenues().stream()
//                            .map(LocationVenueImpl::getName)
//                            .min(Comparator.comparingInt(String::length))
//                            .stream()
//                            .findFirst()
//                            .orElse(" ".repeat(1))
//                            .length()
//                    )
//
//                    .build()
//    );

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id = 0L;

    @Setter(value = AccessLevel.PROTECTED)
    private List<String> data;

    @Setter(value = AccessLevel.PROTECTED)
    @Enumerated
    private LocationType type;

    @Nullable
    private String commonName;

    @Nullable
    @OneToOne(targetEntity = LocationVenueImpl.class, cascade = CascadeType.ALL)
    private LocationVenue venue;


    @Override
    public boolean isSet() {
        return this.data != null && !this.data.isEmpty();
    }

    @Override
    public String format(@Nullable String lineDelimiter, @Nullable String dataDelimiter) {
        if (!this.isSet()) {
            return " ";
        }

        if (lineDelimiter == null) {
            lineDelimiter = IncidentLocation.DEFAULT_LINE_DELIMITER;
        }

        StringJoiner joiner = new StringJoiner(lineDelimiter);

        if (this.getCommonName() != null) {
            joiner.add(this.getCommonName());
        }

        joiner.add(this.getRequiredData(dataDelimiter));

        if (this.getVenue() != null) {
            joiner.add(this.getVenue().getDisplayName());
        }

        return joiner.toString();
    }

    public String getRequiredData(@Nullable String dataDelimiter) {
        String delimiter = returnUnlessNull(dataDelimiter, this.getType().getDefaultDataDelimiter());
        return switch (this.getType()) {
            case ADDRESS, MILE_MARKER -> data.get(0) + delimiter + data.get(1);
            default -> String.join(delimiter, data);
        };
    }

    private String returnUnlessNull(@Nullable String userInput, String def) {
        return userInput != null ? userInput : def;
    }

    @Override
    @NotNull
    public String toString() {
        return this.format();
    }

}

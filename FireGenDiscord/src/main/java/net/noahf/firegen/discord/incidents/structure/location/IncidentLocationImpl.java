package net.noahf.firegen.discord.incidents.structure.location;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.noahf.firegen.api.incidents.location.IncidentLocation;
import net.noahf.firegen.api.incidents.location.LocationVenue;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

/**
 * Represents a location of an {@link IncidentImpl Incident}.
 */
@AllArgsConstructor
@Getter
public class IncidentLocationImpl implements IncidentLocation {

    /**
     * Creates a location of type {@link LocationType#CUSTOM} with custom data, the data will be shown as written.
     */
    public IncidentLocationImpl(List<String> custom) {
        this(custom, LocationType.CUSTOM, null, null);
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

    private List<String> data;
    private LocationType type;
    private @Nullable @Setter String commonName;
    private @Nullable @Setter LocationVenue venue;

    @Override
    public boolean isSet() {
        return this.data != null && !this.data.isEmpty();
    }

    @Override
    public String formatAsOneLine() {
        if (!this.isSet()) {
            return " ";
        }

        String main = (this.commonName != null ? this.commonName + ", " : "");
        switch (type) {
            case ADDRESS -> {
                main = main + data.get(0) + " " + data.get(1);
            }
            case MILE_MARKER -> {
                main = main + data.get(0) + " @ " + data.get(1);
            }
            case INTERSECTION -> {
                main = main + String.join(" / ", data);
            }
            case CROSS_STREETS, LATITUDE_LONGITUDE, CUSTOM -> {
                main = main + String.join(", ", data);
            }
        }
        if (venue != null) {
            main = main + ", " + venue.getDisplay();
        }
        return main;
    }

    @Override
    public String[] formatAsMultipleLines() {
        if (!this.isSet()) {
            return new String[]{" "};
        }

        return switch (this.type) {
            
        };
    }
}

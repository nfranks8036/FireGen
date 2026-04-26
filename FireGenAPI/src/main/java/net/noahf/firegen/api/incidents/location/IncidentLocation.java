package net.noahf.firegen.api.incidents.location;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IncidentLocation {

    String DEFAULT_LINE_DELIMITER = ", ";

    List<String> getData();

    LocationType getType();

    String getCommonName();

    void setCommonName(String commonName);

    LocationVenue getVenue();

    void setVenue(LocationVenue venue);

    boolean isSet();


    String format(@Nullable String lineDelimiter, @Nullable String dataDelimiter);

    default String formatL(@Nullable String lineDelimiter) {
        return this.format(lineDelimiter, null);
    }

    default String formatD(@Nullable String dataDelimiter) {
        return this.format(dataDelimiter, null);
    }

    default String format() {
        return this.format(null, null);
    }

}

package net.noahf.firegen.api.incidents.location;

import java.util.List;

public interface IncidentLocation {

    List<String> getData();

    LocationType getType();

    String getCommonName();

    void setCommonName(String commonName);

    LocationVenue getVenue();

    void setVenue(LocationVenue venue);

    boolean isSet();

    String formatAsOneLine();

    String[] formatAsMultipleLines();

}

package net.noahf.firegen.api.incidents.location;

import java.util.List;

public interface IncidentLocation {

    List<String> getLocationData();

    LocationType getLocationType();

    String getCommonName();

    void setCommonName(String commonName);

    LocationVenue getVenue();

    void setVenue(LocationVenue venue);

    boolean isLocationSet();

    String formatAsOneLine();

    String[] formatAsMultipleLines();

}

package net.noahf.firegen.backend.database.structure;

import dev.morphia.annotations.Entity;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Entity
@Getter
public class Location {

    public String streetAddress = null;

    public Double mileMarker = null;

    public List<String> intersection = null;

    public String shownValue = "<UNKNOWN>";

    public List<String> crossStreets = null;
    public String commonName = "";
    public String venue = "";
    public String state = "VA";
    public Integer zipCode = null;

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;

        this.shownValue = streetAddress;
    }

    public void setIntersection(String... intersection) {
        this.intersection = Arrays.stream(intersection).toList();

        this.shownValue = String.join(" / ", intersection);
    }

    public void setMileMarker(String roadName, Double mileMarker) {
        this.mileMarker = mileMarker;
        this.streetAddress = roadName;

        this.shownValue = roadName + " @ " + mileMarker;
    }

    public void setCustomRoad(String custom) {
        this.shownValue = custom;
    }

}

package net.noahf.firegen.backend.database.structure;

import dev.morphia.annotations.Entity;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

@Entity
@Getter
public class Location {

    public LocationType primaryLocationType = LocationType.CUSTOM;

    public String streetAddress = null;

    public Double mileMarker = null;

    public List<String> intersection = null;

    public double[] coordinates = new double[2];

    public String shownValue = "<UNKNOWN>";

    public List<String> crossStreets = null;
    public String commonName = "";
    public String venue = "";
    public String state = "VA";
    public Integer zipCode = null;

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;

        this.shownValue = streetAddress;
        this.primaryLocationType = LocationType.STREET_ADDRESS;
    }

    public void setIntersection(List<String> intersection) {
        this.intersection = intersection;

        this.shownValue = String.join(" / ", intersection);
        this.primaryLocationType = LocationType.INTERSECTION;
    }

    public void setMileMarker(String roadName, Double mileMarker) {
        this.mileMarker = mileMarker;
        this.streetAddress = roadName;

        this.shownValue = roadName + " @ " + mileMarker;
        this.primaryLocationType = LocationType.MILE_MARKER;
    }

    public void setCoordinates(double latitude, double longitude) {
        this.coordinates = new double[2];
        this.coordinates[0] = latitude; this.coordinates[1] = longitude;

        this.shownValue = latitude + (latitude > 0 ? "N" : "S") + ", " + longitude + (longitude > 0 ? "E" : "W");
        this.primaryLocationType = LocationType.COORDINATES;
    }

    public void setCustomRoad(String custom) {
        this.shownValue = custom;
        this.primaryLocationType = LocationType.CUSTOM;
    }

    public enum LocationType {
        COORDINATES,

        STREET_ADDRESS,

        INTERSECTION,

        MILE_MARKER,

        CUSTOM;
    }

    @Override
    public String toString() {
        return "Location{" +
                "primaryLocationType=" + primaryLocationType +
                ", streetAddress='" + streetAddress + '\'' +
                ", mileMarker=" + mileMarker +
                ", intersection=" + intersection +
                ", coordinates=" + Arrays.toString(coordinates) +
                ", shownValue='" + shownValue + '\'' +
                ", crossStreets=" + crossStreets +
                ", commonName='" + commonName + '\'' +
                ", venue='" + venue + '\'' +
                ", state='" + state + '\'' +
                ", zipCode=" + zipCode +
                '}';
    }
}

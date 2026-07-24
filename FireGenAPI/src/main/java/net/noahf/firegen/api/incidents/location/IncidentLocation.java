package net.noahf.firegen.api.incidents.location;

import net.noahf.firegen.api.utilities.StringSelectors;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents a location of an {@link net.noahf.firegen.api.incidents.Incident Incident}
 */
public interface IncidentLocation extends StringSelectors {

    /**
     * When a line delimiter is not selected, it should default to this.
     */
    String DEFAULT_LINE_DELIMITER = ", ";

    /**
     * The 'data' stored in an {@link IncidentLocation} is the list of content that should be interpreted
     * based off the {@link LocationType}. Two lists that look identical may be formatted entirely differently
     * if the {@link LocationType} is set otherwise. <br> <br>
     * For example, if an address is correctly entered as the list ['123', 'Main St'] and the {@link LocationType} is
     * set to {@link LocationType#ADDRESS ADDRESS}, then this should be formatted as '123 Main St'. But, if the
     * {@link LocationType} is incorrectly set to {@link LocationType#MILE_MARKER MILE_MARKER}, then this may be
     * formatted as '123 @ Main St' unintentionally.
     * @return the raw data associated with this incident location, usually represented as bits and pieces of
     *         the formatted text
     */
    List<String> getData();

    /**
     * The type of {@link IncidentLocation} is the basis of how the {@link IncidentLocation#getData() data} will be
     * interpreted when it comes to formatting the text.
     * @return the specific type of location used in this incident.
     */
    LocationType getType();

    /**
     * The common name is one of the pieces of data associated with this {@link IncidentLocation} that is permanently
     * set inside this object. This value represents what the public would call the location, or the value that would
     * indicate to the average person an associated spot at this location. <br> <br>
     * For example, '123 Main St' may be the actual <i>address</i> for the location, but the <i>common name</i> may
     * be 'Fictional Town Hall'. 'Fictional Town Hall' may be what most people located in this fictional town associate
     * with '123 Main St'.
     * @return the common name of the address, typically the name people refer to the address by
     * @see IncidentLocation#setCommonName(String)
     */
    String getCommonName();

    /**
     * This method sets the {@link IncidentLocation#getCommonName() common name} of the incident. View that method for a
     * more detailed explanation on what a common name is.
     * @param commonName sets the common name of this incident to this value.
     * @see IncidentLocation#getCommonName()
     */
    void setCommonName(String commonName);

    /**
     * The venue is one of the pieces of data associated with this {@link IncidentLocation} that is permanently
     * set inside this object. This value represents the general location that the location is set inside of.
     * In real life, this is often the town limits, city limits, county limits, or some other measure of greater
     * location. <br> <br>
     * For example, '123 Main St' is located inside the greater 'Fictional Town' venue. The 'Fictional Town' venue will
     * add a 'Fictional Town, VA' to the end of the address, where it should look like the following:
     * '123 Main St, <b>Fictional Town, VA</b>'. If you'd like, a Venue can also be a county in which the location is
     * contained, such as '123 Main St, <b>Fictional County, VA</b>', or whatever the system that is set up believes
     * would make sense.
     * @return the venue of the address, typically the greater location of the location.
     * @see IncidentLocation#setVenue(LocationVenue)
     */
    LocationVenue getVenue();

    /**
     * This method sets the {@link IncidentLocation#getVenue() venue} of the incident. View that method for a
     * more detailed explanation on what a venue is.
     * @param venue sets the venue of this incident to this value.
     * @see IncidentLocation#getVenue()
     */
    void setVenue(LocationVenue venue);

    /**
     * Determines if there is actually any {@link IncidentLocation#getData() data} stored in the
     * {@link IncidentLocation}.
     * @return {@code true} if the current location has data assigned to it, {@code false} if otherwise
     */
    boolean isSet();

    /**
     * Formats the current location, its data, common name, and venue into one {@link String} usable for displaying
     * data to an end-user.
     * @param lineDelimiter The line delimiter (defaults to {@link IncidentLocation#DEFAULT_LINE_DELIMITER} if
     *                      {@code null}), is what would traditionally separate a line in the real world. For example,
     *                      'Fictional Town Hall, 123 Main St, Fictional County, VA' would look like: <br> <br>
     *                      Fictional Town Hall <br> 123 Main St <br> Fictional Town, VA <br> <br>
     *                      The separator, in this case, can be customized if you do not want line breaks. The example
     *                      above the latter example ('Fictional Town Hall, 123 Main St, Fictional County, VA') would be
     *                      if you set the {@code lineDelimiter} to a {@code ,}. <br>
     * @param dataDelimiter The data delimiter is what would traditionally separate
     *                      the {@link IncidentLocation#getData() data}. This value is typically set dynamically based
     *                      on the {@link IncidentLocation#getType() location type}, but can be overridden here if the
     *                      value is not {@code null}. Data delimiters can vary, but you can view most of them in the
     *                      enum {@link LocationType}.
     * @return the formatted string with the <b>desired</b> parameter delimiters.
     * @see IncidentLocation#format()
     */
    String format(@Nullable String lineDelimiter, @Nullable String dataDelimiter);

    /**
     * Formats the current location, its data, common name, and venue into one {@link String} usable for display
     * data to an end-user. This method assumes you want the default {@code lineDelimiter} and
     * {@code dataDelimiter}. For more information on how to change those and what they do,
     * {@link IncidentLocation#format(String, String) visit the other format method}.
     * @return the formatted string with <b>default</b> parameter delimiters.
     * @see IncidentLocation#format(String, String)
     */
    default String format() {
        return this.format(null, null);
    }

    @Override
    default List<String> asStringSelectors() {
        return List.of(getType().name(), isSet() ? "isSet" : "isNotSet");
    }

}

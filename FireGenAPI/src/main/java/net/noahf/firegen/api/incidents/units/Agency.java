package net.noahf.firegen.api.incidents.units;

import net.noahf.firegen.api.utilities.StringSelectors;

import java.util.List;

/**
 * Represents an agency that usually houses multiple {@link Unit units}.
 */
public interface Agency extends StringSelectors {

    /**
     * @return the agency title, typically the entire legal name of the agency (e.g., 'Blacksburg Fire Department')
     */
    String getTitle();

    /**
     * @return the agency shorthand, typically the abbreviation used by the agency (e.g., 'BFD')
     */
    String getShorthand();

    /**
     * @return the agency formatted text, typically the text used in voice or over text when referring to the agency
     *         (e.g., 'Blacksburg Fire')
     */
    String getFormatted();

    /**
     * @return the physical station in which the agency is located, usually an address (e.g., 'Blacksburg Fire, 200
     *         Progress St NW, Blacksburg, VA')
     */
    String getStation();

    /**
     * @return the {@link AgencyType type} of agency that is represented by this imported object (e.g., 'FIRE')
     */
    AgencyType getType();

    /**
     * @return the list of {@link Unit units} that this agency owns or operates.
     */
    List<Unit> getUnits();

    /**
     * @return the numeric order this agency is in compared to other agencies (starting at '0')
     */
    int ordinal();


    @Override
    default List<String> asStringSelectors() {
        return List.of(getTitle(), getShorthand(), getFormatted(), getStation(), String.valueOf(ordinal()));
    }

}

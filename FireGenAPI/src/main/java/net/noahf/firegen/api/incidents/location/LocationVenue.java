package net.noahf.firegen.api.incidents.location;

import net.noahf.firegen.api.Identifiable;
import net.noahf.firegen.api.utilities.AutofilledCharSequence;
import net.noahf.firegen.api.utilities.StringSelectors;

import java.util.List;

/**
 * Represents a venue of an {@link net.noahf.firegen.api.incidents.location.IncidentLocation IncidentLocation}
 */
public interface LocationVenue extends Identifiable, AutofilledCharSequence, StringSelectors {

    /**
     * @return the name of the Venue (such as 'VIRGINIA TECH' or 'TOWN OF BLACKSBURG')
     */
    String getName();

    /**
     * @return the display name that would be added to the end of an address (such as 'Blacksburg, VA' or
     *         'Montgomery County, VA')
     */
    String getDisplayName();

    @Override
    default List<String> asStringSelectors() {
        return List.of(getName(), getDisplayName());
    }

}

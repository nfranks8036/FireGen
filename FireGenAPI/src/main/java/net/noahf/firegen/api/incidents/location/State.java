package net.noahf.firegen.api.incidents.location;

import net.noahf.firegen.api.utilities.StringSelectors;

import java.util.List;

/**
 * Represents the state that the {@link net.noahf.firegen.api.incidents.SystemMunicipality} is located in.
 */
public interface State extends StringSelectors {

    /**
     * @return the name of the state (e.g., 'Virginia')
     */
    String getName();

    /**
     * @return the abbreviation for the current state (e.g., 'VA')
     */
    String getAbbreviation();

    @Override
    default List<String> asStringSelectors() {
        return List.of(getName(), getAbbreviation());
    }
}

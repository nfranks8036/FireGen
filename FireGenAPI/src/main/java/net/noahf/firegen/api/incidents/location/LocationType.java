package net.noahf.firegen.api.incidents.location;

import net.noahf.firegen.api.StringSelectors;

import java.util.List;

/**
 * Represents the type of Location inputs that are available to be saved inside an {@link net.noahf.firegen.api.incidents.Incident}.
 * After loading at compile-time, this is entirely customizable by the end-developer.
 */
public interface LocationType extends StringSelectors {

    String getPrefix();

    String getId();

    String getTitle();

    String getShortName();

    String getDescription();

    String getDefaultDataDelimiter();

    LocationField[] getFields();

    @Override
    default List<String> asStringSelectors() {
        return List.of(this.getTitle());
    }

}

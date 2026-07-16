package net.noahf.firegen.api.incidents.location;

import net.noahf.firegen.api.utilities.StringSelectors;

import java.util.List;

public interface State extends StringSelectors {

    String getName();

    String getAbbreviation();

    @Override
    default List<String> asStringSelectors() {
        return List.of(getName(), getAbbreviation());
    }
}

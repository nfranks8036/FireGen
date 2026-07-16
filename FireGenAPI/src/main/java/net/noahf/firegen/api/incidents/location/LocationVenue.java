package net.noahf.firegen.api.incidents.location;

import net.noahf.firegen.api.Identifiable;
import net.noahf.firegen.api.utilities.AutofilledCharSequence;
import net.noahf.firegen.api.utilities.StringSelectors;

import java.util.List;

public interface LocationVenue extends Identifiable, AutofilledCharSequence, StringSelectors {

    String getName();

    String getDisplayName();

    @Override
    default List<String> asStringSelectors() {
        return List.of(getName(), getDisplayName());
    }

}

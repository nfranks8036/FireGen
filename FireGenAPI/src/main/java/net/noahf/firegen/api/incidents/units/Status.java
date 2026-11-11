package net.noahf.firegen.api.incidents.units;

import net.noahf.firegen.api.utilities.AutofilledCharSequence;
import net.noahf.firegen.api.utilities.StringSelectors;

import java.util.List;

public interface Status extends AutofilledCharSequence, StringSelectors {

    String getName();

    String getShortName();

    @Override
    default List<String> asStringSelectors() {
        return List.of(getName(), getShortName());
    }

}

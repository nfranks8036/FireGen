package net.noahf.firegen.api.incidents.units;

import net.noahf.firegen.api.Identifiable;
import net.noahf.firegen.api.utilities.AutofilledCharSequence;
import net.noahf.firegen.api.utilities.StringSelectors;

import java.util.List;

public interface RadioChannel extends Identifiable, AutofilledCharSequence, StringSelectors {

    String getName();

    String getAlphaTag();

    int getTalkgroupId();


    @Override
    default List<String> asStringSelectors() {
        return List.of(getName(), getAlphaTag(), String.valueOf(getTalkgroupId()));
    }

}

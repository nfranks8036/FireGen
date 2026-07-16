package net.noahf.firegen.api.incidents.units;

import net.noahf.firegen.api.Identifiable;
import net.noahf.firegen.api.utilities.AutofilledCharSequence;
import net.noahf.firegen.api.utilities.StringSelectors;

import java.util.List;

public interface Unit extends Identifiable, AutofilledCharSequence, StringSelectors {

    Agency getAgency();

    String getShorthand();

    String getLonghand();

    String getFormatted();

    int ordinal();


    @Override
    default List<String> asStringSelectors() {
        return List.of(getShorthand(), getLonghand(), getFormatted(), String.valueOf(ordinal()));
    }

}

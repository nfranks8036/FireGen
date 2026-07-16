package net.noahf.firegen.api.incidents.units;

import net.noahf.firegen.api.utilities.StringSelectors;

import java.util.List;

public interface Agency extends StringSelectors {

    String getTitle();

    String getShorthand();

    String getFormatted();

    String getStation();

    AgencyType getType();

    List<Unit> getUnits();

    int ordinal();


    @Override
    default List<String> asStringSelectors() {
        return List.of(getTitle(), getShorthand(), getFormatted(), getStation(), String.valueOf(ordinal()));
    }

}

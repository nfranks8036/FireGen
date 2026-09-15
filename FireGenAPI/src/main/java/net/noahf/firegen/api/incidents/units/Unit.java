package net.noahf.firegen.api.incidents.units;

import net.noahf.firegen.api.Identifiable;
import net.noahf.firegen.api.utilities.AutofilledCharSequence;
import net.noahf.firegen.api.utilities.Descriptor;
import net.noahf.firegen.api.utilities.StringSelectors;

import java.util.List;

public interface Unit extends Identifiable, AutofilledCharSequence, StringSelectors, Descriptor {

    Agency getAgency();

    UnitType getType();

    int getNumber();

    String getShorthand();

    String getLonghand();

    String getFormatted();

    Object getAdditional();

    int ordinal();


    @Override
    default List<String> asStringSelectors() {
        return List.of(getShorthand(), getLonghand(), getFormatted(), String.valueOf(ordinal()));
    }

    @Override
    default String describe() {
        return getLonghand() + " (`" + getShorthand() + "`) [`" + getType().getId().replace("_", " ") + "`]";
    }

    @Override
    default boolean shouldDescribe() {
        return ordinal() >= 0;
    }
}

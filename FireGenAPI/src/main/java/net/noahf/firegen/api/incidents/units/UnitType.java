package net.noahf.firegen.api.incidents.units;

import net.noahf.firegen.api.utilities.AutofilledCharSequence;
import net.noahf.firegen.api.utilities.StringSelectors;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface UnitType extends AutofilledCharSequence, StringSelectors {

    String getId();

    @Nullable String getShorthand();

    @Nullable String getLonghand();

    @Nullable String getFormatted();

    String getDescription();


    default String asShorthand(int number) {
        return getShorthand() + number;
    }

    default String asLonghand(int number) {
        return getLonghand() + " " + number;
    }

    default String asFormatted(int number) {
        return getFormatted() + " " + number;
    }


    @Override
    default List<String> asStringSelectors() {
        return List.of(getId());
    }
}

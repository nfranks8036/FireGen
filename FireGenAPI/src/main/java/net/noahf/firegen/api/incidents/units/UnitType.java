package net.noahf.firegen.api.incidents.units;

import net.noahf.firegen.api.utilities.AutofilledCharSequence;
import net.noahf.firegen.api.utilities.Descriptor;
import net.noahf.firegen.api.utilities.StringSelectors;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public interface UnitType extends AutofilledCharSequence, StringSelectors, Descriptor {

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

    @Override
    default String describe() {
        String extra = "";
        if (getShorthand() != null || getLonghand() != null) {
            extra = " (" + String.join(", ",
                    Stream.of("short `"+getShorthand()+"`", "long `" + getLonghand() + "`")
                            .filter(s -> !s.contains("null")).toList()
            ) + ")";
        }
        return getId().replace("_", " ") + extra;
    }
}

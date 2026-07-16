package net.noahf.firegen.api.incidents.units;

import net.noahf.firegen.api.utilities.AutofilledCharSequence;
import net.noahf.firegen.api.utilities.StringSelectors;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface AssignmentStatus extends AutofilledCharSequence, StringSelectors {

    String getName();

    String getShortName();

    int ordinal();

    @Nullable AssignmentPurpose getPurpose();

    @Override
    default List<String> asStringSelectors() {
        return List.of(getName(), getShortName(), String.valueOf(ordinal()));
    }

}

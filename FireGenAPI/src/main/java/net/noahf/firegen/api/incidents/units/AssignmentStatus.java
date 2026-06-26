package net.noahf.firegen.api.incidents.units;

import net.noahf.firegen.api.utilities.AutofilledCharSequence;
import org.jetbrains.annotations.Nullable;

public interface AssignmentStatus extends AutofilledCharSequence {

    String getName();

    String getShortName();

    int ordinal();

    @Nullable AssignmentPurpose getPurpose();

}

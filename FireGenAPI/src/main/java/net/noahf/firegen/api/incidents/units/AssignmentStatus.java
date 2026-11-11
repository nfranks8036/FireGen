package net.noahf.firegen.api.incidents.units;

import net.noahf.firegen.api.utilities.AutofilledCharSequence;
import net.noahf.firegen.api.utilities.StringSelectors;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface AssignmentStatus extends Status {

    int ordinal();

    @Nullable AssignmentPurpose getPurpose();

    List<Secondary> getSecondaries();

}

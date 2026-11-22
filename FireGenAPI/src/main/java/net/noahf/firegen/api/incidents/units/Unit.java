package net.noahf.firegen.api.incidents.units;

import net.noahf.firegen.api.AutofilledCharSequence;
import net.noahf.firegen.api.Descriptor;
import net.noahf.firegen.api.Identifiable;
import net.noahf.firegen.api.StringSelectors;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public interface Unit extends Identifiable, AutofilledCharSequence, StringSelectors, Descriptor, Comparable<Unit> {

    Agency getAgency();

    UnitType getType();

    @Nullable UnitAssignment getCurrentAssignment();

    Set<UnitAssignment> getPreviousAssignments();

    void assign(UnitAssignment assignment);

    OperationalStatus getOperationalStatus();

    void setOperationalStatus(OperationalStatus status);

    long getStatusLastUpdate();

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
        String type = getType().getId();
        return getLonghand() + " (`" + getShorthand() + "`)" + (!type.contains("$") ? " [`" + getType().getId().replace("_", " ") + "`]" : "");
    }

    @Override
    default boolean shouldDescribe() {
        return ordinal() >= 0;
    }
}

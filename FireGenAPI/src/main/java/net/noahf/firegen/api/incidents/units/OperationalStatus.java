package net.noahf.firegen.api.incidents.units;

import org.jetbrains.annotations.Nullable;

public interface OperationalStatus extends Status {

    int ordinal();

    @Nullable OperationalStatusPurpose getPurpose();

}

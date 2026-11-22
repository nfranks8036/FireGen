package net.noahf.firegen.api.incidents.units;

import net.noahf.firegen.api.style.StyledObject;
import org.jetbrains.annotations.Nullable;

public interface OperationalStatus extends Status, StyledObject {

    int ordinal();

    @Nullable OperationalStatusPurpose getPurpose();

}

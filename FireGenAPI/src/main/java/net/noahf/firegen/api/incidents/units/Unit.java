package net.noahf.firegen.api.incidents.units;

import net.noahf.firegen.api.Identifiable;
import net.noahf.firegen.api.utilities.AutofilledCharSequence;

import java.util.List;

public interface Unit extends Identifiable, AutofilledCharSequence {

    AgencyType getAgencyType();

    String getShorthand();

    String getLonghand();

    String getFormatted();

    int ordinal();

}

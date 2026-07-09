package net.noahf.firegen.api.incidents.units;

import net.noahf.firegen.api.Identifiable;
import net.noahf.firegen.api.utilities.AutofilledCharSequence;

public interface Unit extends Identifiable, AutofilledCharSequence {

    Agency getAgency();

    String getShorthand();

    String getLonghand();

    String getFormatted();

    int ordinal();

}

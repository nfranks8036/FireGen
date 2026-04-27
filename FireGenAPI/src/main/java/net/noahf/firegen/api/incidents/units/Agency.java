package net.noahf.firegen.api.incidents.units;

import net.noahf.firegen.api.Identifiable;
import net.noahf.firegen.api.utilities.AutofilledCharSequence;

import java.util.List;

public interface Agency extends Identifiable, AutofilledCharSequence {

    AgencyType getType();

    String getShorthand();

    String getLonghand();

    String getFormatted();

    List<Unit> getUnits();

}

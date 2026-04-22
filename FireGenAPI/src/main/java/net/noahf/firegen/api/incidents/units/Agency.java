package net.noahf.firegen.api.incidents.units;

import net.noahf.firegen.api.Identifiable;

import java.util.List;

public interface Agency extends Identifiable {

    AgencyType getType();

    String getShorthand();

    String getLonghand();

    String getFormatted();

    List<Unit> getUnits();

}

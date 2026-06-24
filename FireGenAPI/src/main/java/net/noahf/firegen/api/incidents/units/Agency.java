package net.noahf.firegen.api.incidents.units;

import java.util.List;

public interface Agency {

    String getTitle();

    String getShorthand();

    String getFormatted();

    String getStation();

    AgencyType getType();

    List<Unit> getUnits();

    int ordinal();



}

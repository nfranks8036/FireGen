package net.noahf.firegen.api.incidents.units;

import net.noahf.firegen.api.Identifiable;

public interface RadioChannel extends Identifiable {

    String getName();

    String getAlphaTag();

    int getTalkgroupId();


}

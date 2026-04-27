package net.noahf.firegen.api.incidents;

import net.noahf.firegen.api.incidents.location.State;

public interface SystemMunicipality {

    String getName();

    String getShortName();

    String getDispatchName();

    State getState();


}

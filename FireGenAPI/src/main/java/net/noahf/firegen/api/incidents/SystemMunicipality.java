package net.noahf.firegen.api.incidents;

import net.noahf.firegen.api.incidents.location.State;
import net.noahf.firegen.api.utilities.Descriptor;

public interface SystemMunicipality extends Descriptor {

    String getName();

    String getShortName();

    String getDispatchName();

    State getState();

    @Override
    default String describe() {
        return "Municipality: " + getName() + " (" + getShortName() + ")\n" +
                "Dispatch: " + getDispatchName() + "\n" +
                "State: " + getState().getName() + " (" + getState().getAbbreviation() + ")";
    }

}

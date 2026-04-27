package net.noahf.firegen.discord.incidents;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.noahf.firegen.api.incidents.SystemMunicipality;
import net.noahf.firegen.api.incidents.location.State;

@Getter
@RequiredArgsConstructor
public class SystemMunicipalityImpl implements SystemMunicipality {

    private final String name;
    private final String dispatchName;
    private final State state;

    @Getter
    @AllArgsConstructor
    @ToString(of = {"name"})
    public static class StateImpl implements State {
        private final String name, abbreviation;
    }

    @Override
    public String toString() {
        return "SystemMunicipality(name=" + name + ", dispatch=" + dispatchName + ", state=" + state.getName() + ")";
    }
}

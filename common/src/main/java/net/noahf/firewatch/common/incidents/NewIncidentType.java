package net.noahf.firewatch.common.incidents;

import net.noahf.firewatch.common.loader.DynamicEnumeration;

public class NewIncidentType implements DynamicEnumeration {

    private String name;
    private String supported_priorities;

    @Override
    public String name() {
        return this.name;
    }

    public String getSupportedPriorities() {
        return this.supported_priorities;
    }

    @Override
    public String toString() {
        return this.name();
    }
}

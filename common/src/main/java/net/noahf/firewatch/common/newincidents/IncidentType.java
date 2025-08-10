package net.noahf.firewatch.common.newincidents;

import net.noahf.firewatch.common.newincidents.objects.StructureList;
import net.noahf.firewatch.common.newincidents.objects.StructureObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IncidentType implements StructureObject {

    static final Set<IncidentPriority> allPriorities = new HashSet<>();

    private String name;
    private boolean ems;
    private List<String> priorities;

    private List<IncidentPriority> incidentPriorities;


    @Override public String getName() { return this.name; }
    @Override public String getFormatted() { return this.name.replace("_", " "); }

    public boolean isEms() { return this.ems; }

    public StructureList<IncidentPriority> getIncidentPriorities() {
        return new StructureList<>(this.incidentPriorities);
    }

    @Override
    public String toString() {
        return this.getName().replace("_", " ");
    }

}

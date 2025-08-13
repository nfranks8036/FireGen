package net.noahf.firewatch.common.data;

import net.noahf.firewatch.common.data.ems.EmsFieldDetails;
import net.noahf.firewatch.common.data.ems.EmsMedical;
import net.noahf.firewatch.common.data.objects.ListMark;
import net.noahf.firewatch.common.data.objects.StructureList;
import net.noahf.firewatch.common.data.objects.StructureObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IncidentType extends StructureObject {

    static final Set<IncidentPriority> allPriorities = new HashSet<>();

    private String name;
    private boolean ems;
    private List<String> priorities;

    private EmsMedical emsPriorities;
    private List<IncidentPriority> incidentPriorities;


    @Override public String name() { return this.name; }
    @Override public String formatted() { return this.name.replace("_", " "); }

    public boolean isEms() { return this.ems; }
    public EmsMedical ems() {
        if (!this.isEms()) {
            throw new IllegalStateException("Incident is not of type 'EMS'");
        }
        if (this.emsPriorities == null) {
//            this.emsPriorities = new EmsMedical(
//                    // TO BE CONTINUED
//            );
        }
        return this.emsPriorities;
    }

    @SuppressWarnings("unchecked")
    public StructureList<IncidentPriority> getIncidentPriorities() {
        return new StructureList<>(this.incidentPriorities, (ListMark<IncidentPriority>) null);
    }

    @Override
    public String toString() {
        return this.name().replace("_", " ");
    }

    public void finalizeDeserialize() {
        if (this.incidentPriorities != null) {
            throw new IllegalStateException("Already finalized and posted.");
        }

        this.incidentPriorities = new ArrayList<>();
        for (String p : this.priorities) {
            IncidentPriority incidentPriority = new IncidentPriority();
            incidentPriority.setName(p);

            incidentPriorities.add(incidentPriority);
            allPriorities.add(incidentPriority);
        }
    }

}
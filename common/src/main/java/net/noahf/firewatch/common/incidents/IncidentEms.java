package net.noahf.firewatch.common.incidents;

import com.sun.tools.javac.Main;
import net.noahf.firewatch.common.FireGen;
import net.noahf.firewatch.common.data.ems.EmsField;
import net.noahf.firewatch.common.data.ems.EmsFieldListItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IncidentEms {

    private final Map<EmsField, EmsFieldListItem> values;

    public IncidentEms() {
        this.values = new HashMap<>();
        for (EmsField field : FireGen.get().incidentStructure().emsMedical().items()) {
            this.values.put(field, null);
        }
    }

    public void set(EmsField field, EmsFieldListItem value) {
        this.values.put(field, value);
    }

    public EmsFieldListItem get(EmsField field) {
        return this.values.get(field);
    }

    public List<EmsField> fields() {
        return new ArrayList<>(this.values.keySet());
    }

}

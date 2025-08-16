package net.noahf.firewatch.common.data.ems;

import net.noahf.firewatch.common.data.objects.StructureList;
import net.noahf.firewatch.common.data.objects.StructureObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class EmsMedical extends StructureObject {

    private List<EmsField> fields;

    @Override public String name() { return "EMS_FIELDS"; }
    @Override public String formatted() { return this.name(); }

    public StructureList<EmsField> fields() {
        return new StructureList<>(this.fields);
    }

}

package net.noahf.firewatch.common.data.ems;

import net.noahf.firewatch.common.data.objects.StructureList;
import net.noahf.firewatch.common.data.objects.StructureObject;

import java.util.List;

@SuppressWarnings("unchecked")
public class EmsField extends StructureObject {

    private String name;
    private List<EmsField> items;

    @Override public String name() { return this.name; }
    @Override public String formatted() { return this.name.replace("_", " "); }

    public StructureList<EmsField> items() { return new StructureList<>(this.items); }

}

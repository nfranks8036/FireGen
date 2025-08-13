package net.noahf.firewatch.common.data.objects;

import java.util.List;
import java.util.function.Predicate;

public class StructureListMarkedObject<T> {

    private final String id;
    private final List<T> objs;

    StructureListMarkedObject(String name, List<T> objs) {
        this.id = name;
        this.objs = objs;
    }

    public String id() { return this.id; }

    public T one() { return this.objs.getFirst(); }
    public List<T> all() { return this.objs; }

}

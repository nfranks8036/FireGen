package net.noahf.firewatch.common.newincidents.lists;

import java.util.Collection;
import java.util.function.Predicate;

@SuppressWarnings("unchecked")
public class StructureList<T extends StructureObject> {

    private final Collection<T> objs;

    public StructureList(Collection<T> objs) {
        this.objs = objs;
    }

    public Collection<T> asCollection() { return this.objs; }
    public T[] asArray() { return (T[]) this.objs.toArray(Object[]::new); }

    public String[] asNames() {
        return this.objs.stream()
            .map(StructureObject::getName).toArray(String[]::new);
    }
    public String[] asFormatted() {
        return this.objs.stream()
            .map(StructureObject::getFormatted).toArray(String[]::new);
    }

    public T getFromName(String name) {
        for (T obj : this.asCollection()) {
            if (obj.getName().equalsIgnoreCase(name)) {
                return obj;
            }
        }
        return null;
    }

    public T getFromFormatted(String format) {
        for (T obj : this.asCollection()) {
            if (obj.getFormatted().equalsIgnoreCase(format)) {
                return obj;
            }
        }
        return null;
    }

    public int count() {
        return this.objs.size();
    }

    public boolean contains(T object) {
        return this.objs.contains(object);
    }

    public StructureList<T> filter(Predicate<T> filter) {
        return new StructureList<>(this.objs.stream().filter(filter).toList());
    }

}

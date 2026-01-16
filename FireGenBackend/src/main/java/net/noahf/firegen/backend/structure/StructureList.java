package net.noahf.firegen.backend.structure;

import io.smallrye.common.constraint.NotNull;
import lombok.Getter;

import java.util.*;

/**
 * Represents a list involving structure objects, see a list of all of these in {@link StructureManager}
 * @param <T> the type of objects for this list to hold
 */
public class StructureList<T extends StructureObject> implements Iterable<T> {

    private final @Getter Class<T> type;
    private final @NotNull Collection<T> objects;

    StructureList(Class<T> type, @NotNull Collection<T> objects) {
        this.type = type;
        this.objects = objects;
    }

    /**
     * <strong>NOT PREFERRED VERSION!</strong>
     * Attempts to guess the type of the list by checking the first element.
     * @param objects the objects to add to the list
     */
    @SuppressWarnings("unchecked")
    StructureList(@NotNull Collection<T> objects) {
        this.type = (Class<T>) new ArrayList<>(objects).get(0).getClass();
        this.objects = objects;
    }

    /**
     * Returns this list as a Java ArrayList
     * @return the java list variant
     */
    public List<T> asList() {
        return new ArrayList<>(this.objects);
    }

    /**
     * Returns this list only with the name of the objects here.
     * @return the names of all the items in the list.
     */
    public List<String> asNameList() {
        List<String> list = new ArrayList<>();
        for (T obj : this.objects) {
            list.add(obj.getName());
        }
        return list;
    }

    /**
     * @param name the name attributed to an object in this list
     * @return the object associated with this name, or {@code null} if no object was found.
     */
    public T from(String name) {
        T returned = null;
        for (T obj : this.objects) {
            if (obj.getName().equalsIgnoreCase(name)) {
                returned = obj; break;
            }
        }
        return returned;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(", ", this.getType().getSimpleName() + "[", "]");
        for (T obj : this.asList()) {
            joiner.add(obj.toString());
        }
        return joiner.toString();
    }

    @Override
    public Iterator<T> iterator() {
        return this.objects.iterator();
    }
}

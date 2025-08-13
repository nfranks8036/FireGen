package net.noahf.firewatch.common.data.objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

@SuppressWarnings("unchecked")
public class StructureList<T extends StructureObject> implements Iterable<T> {

    private final Collection<T> objs;
    private final List<StructureListMarkedObject<T>> markedObjects;

    public StructureList(Collection<T> objs, ListMark<T>... markedObjects) {
        this(objs, (i) -> i);
    }

    public <R> StructureList(Collection<R> objs, Function<? super R, T> mapper) {
        this(objs, mapper, (ListMark<R>) null);
    }

    public <R> StructureList(Collection<R> objs, Function<? super R, T> mapper, ListMark<R>... markedObjects) {
        this.objs = objs.stream().map(mapper).toList();

        if (markedObjects != null && markedObjects.length > 0) {
            this.markedObjects = new ArrayList<>();
            for (ListMark<R> mark : markedObjects) {
                if (mark == null) {
                    continue;
                }

                StructureListMarkedObject<T> markedObject = new StructureListMarkedObject<>(mark.name, objs.stream().filter(mark.filter).map(mapper).toList());
                if (markedObject.all().size() > mark.max) {
                    System.err.println("ListMark has reached max (" + markedObject.all().size() + " > " + mark.max + ") for filter '" + mark.name + "'");
                    continue;
                }
                this.markedObjects.add(markedObject);
            }
        } else this.markedObjects = null;
    }

    private StructureList(Collection<T> objs, StructureList<T> original) {
        this.objs = objs;
        this.markedObjects = original.markedObjects;
    }

    public Collection<T> asCollection() { return this.objs; }
    public T[] asArray() { return (T[]) this.objs.toArray(Object[]::new); }

    public String[] asNames() {
        return this.objs.stream()
                .map(StructureObject::name).toArray(String[]::new);
    }
    public String[] asFormatted() {
        return this.objs.stream()
                .map(StructureObject::formatted).toArray(String[]::new);
    }

    public T getFromName(String name) {
        for (T obj : this.asCollection()) {
            if (obj.name().equalsIgnoreCase(name)) {
                return obj;
            }
        }
        return null;
    }

    public T getFromFormatted(String format) {
        for (T obj : this.asCollection()) {
            if (obj.formatted().equalsIgnoreCase(format)) {
                return obj;
            }
        }
        return null;
    }

    public StructureListMarkedObject<T> marked(String id) {
        return this.markedObjects.stream().filter(obj -> obj.id().equalsIgnoreCase(id)).findFirst().orElse(null);
    }

    public int count() {
        return this.objs.size();
    }

    public boolean contains(T object) {
        return this.objs.contains(object);
    }

    public StructureList<T> filter(Predicate<T> filter) {
        return new StructureList<>(this.objs.stream().filter(filter).toList(), this);
    }




    @Override
    public @NotNull Iterator<T> iterator() {
        return this.objs.iterator();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        this.objs.forEach(action);
    }

    @Override
    public Spliterator<T> spliterator() {
        return this.objs.spliterator();
    }
}

package net.noahf.firewatch.common.utils;

import java.util.Arrays;
import java.util.List;

public class ObjectDuplicator<T> {

    private final T[] objs;

    @SafeVarargs
    public ObjectDuplicator(T... objs) {
        this.objs = objs;
    }

    public T[] duplicate() {
        return this.duplicate(2);
    }

    public T[] duplicate(int times) {
        T[] newObjs = Arrays.copyOf(this.objs, this.objs.length * times);
        for (int i = 0; i < objs.length; i++) {
            for (int time = 0; time < times; time++) {
                newObjs[i + (objs.length * time)] = objs[i];
            }
        }
        return newObjs;
    }

    public List<T> duplicateToList() {
        return this.duplicateToList(2);
    }

    public List<T> duplicateToList(int times) {
        return Arrays.stream(this.duplicate(times)).toList();
    }

}

package net.noahf.firewatch.common.data.objects;

import java.util.function.Predicate;

public class ListMark<T> {

    public static <T> ListMark<T> of(String name, Predicate<T> filter, int max) {
        return new ListMark<>(name, filter, max);
    }

    public static <T> ListMark<T> of(String name, Predicate<T> filter) {
        return ListMark.of(name, filter, 1);
    }

    final String name;
    final Predicate<T> filter;
    final int max;

    ListMark(String name, Predicate<T> filter, int max) {
        this.name = name;
        this.filter = filter;
        this.max = max;
    }

}

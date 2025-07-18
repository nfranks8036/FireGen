package net.noahf.firewatch.desktopclient.utils;

public class ObjectDuplicator<T> {

    private final T[] objs;

    @SafeVarargs
    public ObjectDuplicator(T... objs) {
        this.objs = objs;
    }

    public T[] duplicate() {
        return this.duplicate(1);
    }

    @SuppressWarnings("unchecked")
    public T[] duplicate(int times) {
        T[] newObjs = (T[]) new Object[objs.length * times];
        for (int i = 0; i < objs.length; i++) {
            for (int time = 0; time < times; time++) {
                newObjs[i + (objs.length * time)] = objs[i];
            }
        }
        return newObjs;
    }

}

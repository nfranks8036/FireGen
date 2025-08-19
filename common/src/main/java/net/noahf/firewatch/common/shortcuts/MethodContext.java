package net.noahf.firewatch.common.shortcuts;

public class MethodContext<T> {

    T primaryObject;

    public MethodContext(T primaryObject) {
        this.primaryObject = primaryObject;
    }

}

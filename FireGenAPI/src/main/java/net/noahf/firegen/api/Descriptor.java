package net.noahf.firegen.api;

public interface Descriptor {

    String describe();

    default boolean shouldDescribe() { return true; }

}

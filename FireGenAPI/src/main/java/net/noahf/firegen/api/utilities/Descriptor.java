package net.noahf.firegen.api.utilities;

public interface Descriptor {

    String describe();

    default boolean shouldDescribe() { return true; }

}

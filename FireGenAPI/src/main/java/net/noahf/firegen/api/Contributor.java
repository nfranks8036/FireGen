package net.noahf.firegen.api;

import net.noahf.firegen.api.utilities.AutofilledCharSequence;

public interface Contributor<T> extends Identifiable, AutofilledCharSequence {

    String getName();

    String getDisplayName();

    T getUserObject();

}

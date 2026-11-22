package net.noahf.firegen.api;

import java.util.List;

public interface Contributor<T> extends Identifiable, AutofilledCharSequence, StringSelectors {

    String getUsername();

    String getLoginUsername();

    long getId();

    T getUserObject();

    @Override
    default List<String> asStringSelectors() {
        return List.of(getUsername(), getLoginUsername(), String.valueOf(getId()));
    }

}

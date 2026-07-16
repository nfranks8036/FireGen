package net.noahf.firegen.api;

import net.noahf.firegen.api.utilities.AutofilledCharSequence;
import net.noahf.firegen.api.utilities.StringSelectors;

import java.util.List;

public interface Contributor<T> extends Identifiable, AutofilledCharSequence, StringSelectors {

    String getName();

    String getDisplayName();

    long getId();

    T getUserObject();

    @Override
    default List<String> asStringSelectors() {
        return List.of(getName(), getDisplayName(), String.valueOf(getId()));
    }

}

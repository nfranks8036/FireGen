package net.noahf.firegen.api;

import net.noahf.firegen.api.utilities.AutofilledCharSequence;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface Contributor<T> extends Identifiable, AutofilledCharSequence {

    String getName();

    String getDisplayName();

    T getUserObject();

}

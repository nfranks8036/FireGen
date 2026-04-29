package net.noahf.firegen.api.incidents.location;

import net.noahf.firegen.api.Identifiable;
import net.noahf.firegen.api.utilities.AutofilledCharSequence;

public interface LocationVenue extends Identifiable, AutofilledCharSequence {

    String getName();

    String getDisplayName();

}

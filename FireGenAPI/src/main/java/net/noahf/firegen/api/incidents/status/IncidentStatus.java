package net.noahf.firegen.api.incidents.status;

import net.noahf.firegen.api.utilities.AutofilledCharSequence;

public interface IncidentStatus extends AutofilledCharSequence {

    String getName();

    String getShortName();

    IncidentStatusAttributes getAttributes();

}

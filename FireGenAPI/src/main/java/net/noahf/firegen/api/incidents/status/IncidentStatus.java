package net.noahf.firegen.api.incidents.status;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import net.noahf.firegen.api.utilities.AutofilledCharSequence;

public interface IncidentStatus extends AutofilledCharSequence {

    String getName();

    String getShortName();

    IncidentStatusAttributes getAttributes();

}

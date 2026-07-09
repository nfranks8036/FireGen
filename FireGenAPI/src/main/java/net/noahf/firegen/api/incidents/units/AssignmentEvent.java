package net.noahf.firegen.api.incidents.units;

import net.noahf.firegen.api.Contributor;
import net.noahf.firegen.api.utilities.AutofilledCharSequence;

import java.time.LocalDateTime;

public interface AssignmentEvent extends AutofilledCharSequence {

    LocalDateTime getTimestamp();

    AssignmentStatus getStatus();

    Contributor<?> getContributor();

}

package net.noahf.firegen.api.incidents.units;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.noahf.firegen.api.Contributor;
import net.noahf.firegen.api.utilities.AutofilledCharSequence;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

public interface AssignmentEvent extends AutofilledCharSequence {

    LocalDateTime getTimestamp();

    AssignmentStatus getStatus();

    Contributor<?> getContributor();

}

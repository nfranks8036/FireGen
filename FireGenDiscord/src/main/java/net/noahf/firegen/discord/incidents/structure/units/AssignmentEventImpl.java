package net.noahf.firegen.discord.incidents.structure.units;

import jakarta.persistence.*;
import jakarta.validation.constraints.Null;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.noahf.firegen.api.Contributor;
import net.noahf.firegen.api.incidents.units.AssignmentEvent;
import net.noahf.firegen.api.incidents.units.AssignmentStatus;
import net.noahf.firegen.discord.users.FireGenUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.StringJoiner;

@RequiredArgsConstructor
@Getter
@Entity
public class AssignmentEventImpl implements AssignmentEvent {

    private @Id @GeneratedValue(strategy = GenerationType.IDENTITY) long id;

    private final LocalDateTime timestamp;

    @OneToOne(targetEntity = AssignmentStatusImpl.class, cascade = CascadeType.ALL)
    private final AssignmentStatus status;

    @OneToOne(targetEntity = FireGenUser.class, cascade = CascadeType.ALL)
    private final Contributor<?> contributor;

    private final @Nullable String secondary;

    @Override
    @NotNull
    public String toString() {
        StringJoiner joiner = new StringJoiner(", ", "[", "]")
                .add("time=" + timestamp.toString())
                .add("status=" + status.toString())
                .add("contributor=" + contributor.toString());
        if (secondary != null) {
            joiner.add("secondary=" + secondary);
        }
        return joiner.toString();
    }
}

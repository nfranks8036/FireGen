package net.noahf.firegen.discord.incidents.structure.units;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.noahf.firegen.api.Contributor;
import net.noahf.firegen.api.incidents.units.AssignmentEvent;
import net.noahf.firegen.api.incidents.units.AssignmentStatus;
import net.noahf.firegen.discord.users.FireGenUser;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

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

    @Override
    @NotNull
    public String toString() {
        return "[time=" + timestamp.toString() + ", status=" + status.toString() + ", contributor=" + contributor.toString() + "]";
    }
}

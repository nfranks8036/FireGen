package net.noahf.firegen.discord.incidents.structure;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.noahf.firegen.api.incidents.status.IncidentStatus;
import net.noahf.firegen.api.incidents.status.IncidentStatusAttributes;
import org.jetbrains.annotations.NotNull;

@Getter
@AllArgsConstructor
@Entity
public class IncidentStatusImpl implements IncidentStatus {

    private @Id @GeneratedValue int id;

    protected IncidentStatusImpl() {
        // required for JPA Hibernate
    }

    private String name;
    private String shortName;
    private String leftEmoji;
    private String rightEmoji;
    private @OneToOne(cascade = CascadeType.ALL) IncidentStatusAttributes attributes;

    public String getEmojisFormattedCombined() {
        return this.leftEmoji + this.rightEmoji;
    }

    @Override
    @NotNull
    public String toString() {
        return this.name;
    }
}

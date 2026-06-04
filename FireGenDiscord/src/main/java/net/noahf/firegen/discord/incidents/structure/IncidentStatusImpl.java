package net.noahf.firegen.discord.incidents.structure;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.noahf.firegen.api.incidents.status.IncidentStatus;
import net.noahf.firegen.api.incidents.status.IncidentStatusAttributes;
import org.jetbrains.annotations.NotNull;

@Getter
@Entity
public class IncidentStatusImpl implements IncidentStatus {

    private @Id @GeneratedValue int id;

    protected IncidentStatusImpl() {
        // required for JPA Hibernate
    }

    public IncidentStatusImpl(String name, String shortName, String leftEmoji, String rightEmoji, IncidentStatusAttributes attributes) {
        this.name = name;
        this.shortName = shortName;
        this.leftEmoji = leftEmoji;
        this.rightEmoji = rightEmoji;
        this.attributes = attributes;
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

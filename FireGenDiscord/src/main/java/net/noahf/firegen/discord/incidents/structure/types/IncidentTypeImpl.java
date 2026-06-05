package net.noahf.firegen.discord.incidents.structure.types;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.noahf.firegen.api.incidents.types.IncidentType;
import net.noahf.firegen.api.incidents.types.IncidentTypeTag;
import net.noahf.firegen.api.utilities.AutofilledCharSequence;
import net.noahf.firegen.api.utilities.IdGenerator;
import org.jetbrains.annotations.NotNull;

@Getter
@Entity
@NoArgsConstructor(force = true)
public class IncidentTypeImpl implements IncidentType, AutofilledCharSequence {

    private @Id final long id;
    private final @NotNull String type;
    private @OneToOne(cascade = CascadeType.ALL, targetEntity = IncidentTypeTagImpl.class) final @NotNull
            IncidentTypeTag tag;
    private final int qualifierChoice;

    public IncidentTypeImpl(@NotNull String type, @NotNull IncidentTypeTag tag, int qualifierChoice) {
        this.id = IdGenerator.generateTypeId(this);
        this.type = type;
        this.tag = tag;
        this.qualifierChoice = qualifierChoice;
    }

    @Override
    public int getPriorityChoice() {
        return Integer.MIN_VALUE;
    }

    @Override
    public String getSelectedName() {
        try {
            return tag.findTypeOptions(this.type).get(this.qualifierChoice);
        } catch (NullPointerException nullPointerException) {
            return null;
        }
    }

    @Override
    @NotNull
    public String toString() {
        if (this.getSelectedName() == null) {
            return "[IncidentType Empty]";
        }

        return this.getSelectedName();
    }
}

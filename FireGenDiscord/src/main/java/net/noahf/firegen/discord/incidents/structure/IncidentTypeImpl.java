package net.noahf.firegen.discord.incidents.structure;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.noahf.firegen.api.incidents.IncidentType;
import net.noahf.firegen.api.incidents.IncidentTypeTag;
import net.noahf.firegen.api.utilities.AutofilledCharSequence;
import net.noahf.firegen.api.utilities.IdGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

@Getter
public class IncidentTypeImpl implements IncidentType, AutofilledCharSequence {

    private final long id;
    private final @NotNull String type;
    private final @NotNull IncidentTypeTag tag;
    private final int qualifierChoice;

    public IncidentTypeImpl(@NotNull String type, @NotNull IncidentTypeTag tag, int qualifierChoice) {
        this.id = IdGenerator.generateTypeId(this);
        this.type = type;
        this.tag = tag;
        this.qualifierChoice = qualifierChoice;
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
    public String getSelectedPriority() {
        return " ";
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

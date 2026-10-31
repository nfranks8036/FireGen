package net.noahf.firegen.discord.incidents.structure.types;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.noahf.firegen.api.incidents.types.IncidentType;
import net.noahf.firegen.api.incidents.types.IncidentTypeTag;
import net.noahf.firegen.api.utilities.AutofilledCharSequence;
import net.noahf.firegen.api.utilities.IdGenerator;
import net.noahf.firegen.discord.utilities.Log;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
@NoArgsConstructor(force = true)
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
    public int getPriorityChoice() {
        return Integer.MIN_VALUE;
    }

    @Override
    public String getStringQualifierChoice() {
        if (qualifierChoice == -1) {
            return "None";
        }
        int select = this.getQualifierChoice();
        if (this.tag.getQualifiers() != null && !this.tag.getQualifiers().isRequired()) {
            select--;
        }
        return tag.getQualifiers().getQualifiers().get(select);
    }

    @Override
    public String getSelectedName() {
        try {
            List<String> options = tag.findTypeOptions(this.type);
            int select = this.qualifierChoice;
            if (select == -1) {
                return getType();
            }
            return options.get(select);
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

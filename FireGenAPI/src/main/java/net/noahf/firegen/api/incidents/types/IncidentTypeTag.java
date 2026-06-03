package net.noahf.firegen.api.incidents.types;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import net.noahf.firegen.api.utilities.AutofilledCharSequence;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IncidentTypeTag extends AutofilledCharSequence {

    String getTagName();

    List<String> getPriorities();

    IncidentTypeTagQualifierList getQualifiers();

    @NotNull String toString();

    List<String> findTypeOptions(String name);

}

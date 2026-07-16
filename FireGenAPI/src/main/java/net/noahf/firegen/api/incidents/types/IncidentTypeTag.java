package net.noahf.firegen.api.incidents.types;

import net.noahf.firegen.api.utilities.AutofilledCharSequence;
import net.noahf.firegen.api.utilities.StringSelectors;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IncidentTypeTag extends AutofilledCharSequence, StringSelectors {

    String getTagName();

    List<String> getPriorities();

    IncidentTypeTagQualifierList getQualifiers();

    @NotNull String toString();

    List<String> findTypeOptions(String name);


    @Override
    default List<String> asStringSelectors() {
        return List.of(getTagName());
    }

}

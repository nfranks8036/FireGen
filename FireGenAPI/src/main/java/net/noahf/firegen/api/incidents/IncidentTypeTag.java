package net.noahf.firegen.api.incidents;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import net.noahf.firegen.api.utilities.AutofilledCharSequence;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IncidentTypeTag extends AutofilledCharSequence {

    String getTagName();

    List<String> getPriorities();

    Qualifier getQualifier();

    @NotNull String toString();



    List<String> findTypeOptions(String name);

    @Getter
    @AllArgsConstructor
    @ToString
    class Qualifier implements AutofilledCharSequence {
        protected boolean required;
        protected boolean unique;
        protected String syntax;
        protected List<String> qualifiers;
    }

}

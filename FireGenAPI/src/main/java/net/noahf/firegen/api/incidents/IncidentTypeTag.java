package net.noahf.firegen.api.incidents;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

public interface IncidentTypeTag {

    String getTagName();

    List<String> getPriorities();

    Qualifier getQualifier();



    List<String> findTypeOptions(String name);

    @Getter
    @AllArgsConstructor
    @ToString
    class Qualifier {
        protected boolean required;
        protected boolean unique;
        protected String syntax;
        protected List<String> qualifiers;
    }

}

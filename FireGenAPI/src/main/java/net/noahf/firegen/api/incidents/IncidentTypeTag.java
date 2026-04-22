package net.noahf.firegen.api.incidents;

import lombok.Getter;

import java.util.List;

public interface IncidentTypeTag {

    String getTagName();

    List<String> getPriorities();

    Qualifier getQualifier();



    List<String> findTypeOptions(IncidentType type);

    @Getter
    class Qualifier {
        protected boolean required;
        protected boolean unique;
        protected String syntax;
        protected List<String> qualifiers;
    }

}

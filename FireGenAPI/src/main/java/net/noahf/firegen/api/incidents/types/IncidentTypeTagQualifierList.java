package net.noahf.firegen.api.incidents.types;

import java.util.List;

public interface IncidentTypeTagQualifierList {

    boolean isRequired();

    boolean isUnique();

    String getSyntax();

    List<String> getQualifiers();

}

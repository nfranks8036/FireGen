package net.noahf.firegen.api.incidents.types;

import lombok.*;
import net.noahf.firegen.api.utilities.AutofilledCharSequence;

import java.util.List;

public interface IncidentTypeTagQualifierList {

    boolean isRequired();

    boolean isUnique();

    String getSyntax();

    List<String> getQualifiers();

}

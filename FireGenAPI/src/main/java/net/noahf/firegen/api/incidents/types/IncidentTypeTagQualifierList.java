package net.noahf.firegen.api.incidents.types;

import java.util.List;

/**
 * Represents a list of incident type qualifiers of a {@link IncidentTypeTag}
 */
public interface IncidentTypeTagQualifierList {

    /**
     * @return {@code true} if one of the few qualifiers are required to be selected, {@code false} if it is valid to
     *         not select any qualifier
     */
    boolean isRequired();

    /**
     * @return {@code true} if the qualifiers have to be unique (e.g., you can't select two or more qualifiers),
     *         {@code false} if it is valid to select multiple qualifiers for one incident
     */
    boolean isUnique();

    /**
     * @return the syntax of how the incident type and incident qualifier(s) should be shown to the user. The
     *         placeholders {@code {T}} (incident type generic name) and {@code {Q}} (qualifier selected) can be used
     *         here.
     */
    String getSyntax();

    /**
     * @return the list of available qualifiers as just their simple name.
     */
    List<String> getQualifiers();

}

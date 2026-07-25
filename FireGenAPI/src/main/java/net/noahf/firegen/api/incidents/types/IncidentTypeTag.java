package net.noahf.firegen.api.incidents.types;

import net.noahf.firegen.api.utilities.AutofilledCharSequence;
import net.noahf.firegen.api.utilities.StringSelectors;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Returns the incident type tag for a {@link IncidentType}
 */
public interface IncidentTypeTag extends AutofilledCharSequence, StringSelectors {

    /**
     * @return the name of the current type tag
     */
    String getTagName();

    /**
     * @return the list of available priorities that a user can select from
     */
    List<String> getPriorities();

    /**
     * @return the list (as an object {@link IncidentTypeTagQualifierList}) of available qualifiers that a user can
     *         select from
     */
    IncidentTypeTagQualifierList getQualifiers();

    /**
     * This method is mainly for importing usage only, but it essentially is meant to provide the base Incident Type
     * name (e.g., 'MVC') and then create a list of strings based on the available qualifiers. For example, if you have
     * the incident type '{@code MVC}' and it is under the type tag '{@code MOTOR_VEHICLE_CRASH}'. The
     * '{@code MOTOR_VEHICLE_CRASH}' tag has the following qualifiers: '{@code INJURIES}', '{@code PROPERTY DAMAGE}', or
     * '{@code ENTRAPMENT}'. Then this method would take in the argument '{@code MVC}' and spit out (given the syntax is
     * '{@code {T} W/ {Q}}'): {@code ["MVC W/ INJURIES", "MVC W/ PROPERTY DAMAGE", "MVC W/ ENTRAPMENT"]}
     * @param genericIncidentType the generic name of the incident type without any qualifiers or priorities
     * @return the list of all available types based on the current type tag and the generic name
     */
    List<String> findTypeOptions(String genericIncidentType);


    @Override
    default List<String> asStringSelectors() {
        return List.of(getTagName());
    }

}

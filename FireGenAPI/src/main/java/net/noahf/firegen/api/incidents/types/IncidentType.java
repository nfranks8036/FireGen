package net.noahf.firegen.api.incidents.types;

import net.noahf.firegen.api.Identifiable;
import net.noahf.firegen.api.utilities.StringSelectors;

import java.util.List;

/**
 * Represents the incident type (call type) of an {@link net.noahf.firegen.api.incidents.Incident}
 */
public interface IncidentType extends Identifiable, StringSelectors {

    /**
     * @return the raw type of the incident. This method will <b>NOT</b> return the entire incident type but
     *         rather just the generic type selected (without qualifiers or priorities). To view the selected String
     *         with qualifiers, see {@link IncidentType#getSelectedName()}.
     */
    String getType();

    /**
     * @return the tag for the incident that was assigned to it. This class is where the qualifiers and
     *         list of priorities will be saved.
     */
    IncidentTypeTag getTag();

    /**
     * @return the selected priority choice, an index from 0 to however many priorities are defined by
     *         {@link IncidentTypeTag}
     * @see IncidentType#getTag()
     */
    int getPriorityChoice();

    /**
     * @return the selected qualifier choice, an index from 0 to however many qualifiers are defined by
     *         {@link IncidentTypeTag}
     * @see IncidentType#getTag()
     */
    int getQualifierChoice();

    /**
     * @return the selected qualifier choice as a string (as defined in {@link IncidentTypeTag}
     * @see IncidentType#getTag()
     */
    String getStringQualifierChoice();

    /**
     * @return the true selected name by the user, this includes qualifiers and priorities.
     */
    String getSelectedName();


    @Override
    default List<String> asStringSelectors() {
        return List.of(getSelectedName());
    }

}

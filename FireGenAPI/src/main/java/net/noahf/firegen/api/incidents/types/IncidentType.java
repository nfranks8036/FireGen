package net.noahf.firegen.api.incidents.types;

import net.noahf.firegen.api.Identifiable;
import net.noahf.firegen.api.utilities.StringSelectors;

import java.util.List;

public interface IncidentType extends Identifiable, StringSelectors {

    String getType();

    IncidentTypeTag getTag();

    int getPriorityChoice();

    int getQualifierChoice();


    String getSelectedName();


    @Override
    default List<String> asStringSelectors() {
        return List.of(getSelectedName(), getType());
    }

}

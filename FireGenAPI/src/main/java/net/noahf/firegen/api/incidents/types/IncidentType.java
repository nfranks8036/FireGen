package net.noahf.firegen.api.incidents.types;

import net.noahf.firegen.api.Identifiable;

public interface IncidentType extends Identifiable {

    String getType();

    IncidentTypeTag getTag();

    int getPriorityChoice();

    int getQualifierChoice();



    String getSelectedName();

}

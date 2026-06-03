package net.noahf.firegen.api.incidents.types;

import net.noahf.firegen.api.Identifiable;

public interface IncidentType extends Identifiable {

    String getType();

    IncidentTypeTag getTag();

    int getQualifierChoice();



    String getSelectedName();

    String getSelectedPriority();

}

package net.noahf.firegen.api.incidents;

import net.noahf.firegen.api.Identifiable;

public interface IncidentType extends Identifiable {

    String type();

    IncidentTypeTag tag();

    int qualifierChoice();



    String getSelectedName();

    String getSelectedPriority();

}

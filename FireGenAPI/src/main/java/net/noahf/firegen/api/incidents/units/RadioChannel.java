package net.noahf.firegen.api.incidents.units;

import net.noahf.firegen.api.Identifiable;
import net.noahf.firegen.api.utilities.AutofilledCharSequence;

public interface RadioChannel extends Identifiable, AutofilledCharSequence {

    String name();

    String alphaTag();

    int talkgroupId();


}

package net.noahf.firegen.api.utilities;

import java.util.List;

public interface StringSelectors {

    /**
     * Converts an object into a List of {@link String strings} that identify the object. This is useful when
     * cycling through a list of objects and trying to find one which matches a string.
     * @return the list of strings which identify the current object.
     */
    List<String> asStringSelectors();

}

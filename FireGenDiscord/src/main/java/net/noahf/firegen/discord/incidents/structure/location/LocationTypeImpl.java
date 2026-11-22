package net.noahf.firegen.discord.incidents.structure.location;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.noahf.firegen.api.incidents.location.LocationField;

@AllArgsConstructor
@Getter
public class LocationTypeImpl implements net.noahf.firegen.api.incidents.location.LocationType {

    private final String prefix;
    private final String id;
    private final String title;
    private final String shortName;
    private final String description;
    private final String defaultDataDelimiter;
    private final LocationField[] fields;

}

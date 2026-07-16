package net.noahf.firegen.api.incidents;

import net.noahf.firegen.api.utilities.StringSelectors;

import java.util.List;

public enum IncidentPublishedStatus implements StringSelectors {

    PUBLISHED,

    UNPUBLISHED,

    UNKNOWN;

    public IncidentPublishedStatus opposite() {
        return switch(this) {
            case PUBLISHED -> UNPUBLISHED;
            case UNPUBLISHED -> PUBLISHED;
            default -> UNKNOWN;
        };
    }

    @Override
    public List<String> asStringSelectors() {
        return List.of(name());
    }

}

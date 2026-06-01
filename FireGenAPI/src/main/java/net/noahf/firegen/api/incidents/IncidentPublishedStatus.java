package net.noahf.firegen.api.incidents;

public enum IncidentPublishedStatus {

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

}

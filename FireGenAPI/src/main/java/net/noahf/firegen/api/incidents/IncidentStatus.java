package net.noahf.firegen.api.incidents;

public enum IncidentStatus {

    ACTIVE("This incident is considered active, which means there are units attached, en-route, and/or actively on-scene."),

    PENDING("This incident is considered pending, which means there are no current units attached or active at this scene."),

    CLOSED("This incident is considered closed, which means no other changes will be made to this incident."),

    TIMED_OUT("This incident is considered timed out, which means no changes were made to this incident, meaning it's likely closed.");


    private final String description;

    IncidentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

}

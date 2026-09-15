package net.noahf.firegen.discord.actions.errors;

import lombok.Getter;

@Getter
public class IncidentNotExist extends RuntimeException {

    private final String gotIncident;

    public IncidentNotExist(String gotIncident) {
        super("An incident does not exist with the Incident Number of '" + gotIncident + "'.");
        this.gotIncident = gotIncident;
    }

}

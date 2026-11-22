package net.noahf.firegen.discord.services;

import lombok.Getter;
import lombok.experimental.Accessors;

@Getter @Accessors(fluent = true)
public class ServicesManager {

    private final IncidentService incidents = new IncidentService();
    private final IncidentLocationService incidentLocation = new IncidentLocationService();

}

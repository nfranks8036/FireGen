package net.noahf.firegen.api.database;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import net.noahf.firegen.api.Contributor;
import net.noahf.firegen.api.incidents.IncidentPublishedStatus;
import net.noahf.firegen.api.incidents.types.IncidentType;
import net.noahf.firegen.api.incidents.location.IncidentLocation;
import net.noahf.firegen.api.incidents.status.IncidentStatus;

import java.time.LocalDateTime;

@Builder(builderMethodName = "search", buildMethodName = "finish", setterPrefix = "by")
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class IncidentQuery {

    private IncidentType type;
    private IncidentStatus status;
    private IncidentLocation location;
    private Contributor<?> contributor;
    private IncidentPublishedStatus isPublished;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private boolean isOpen;

}

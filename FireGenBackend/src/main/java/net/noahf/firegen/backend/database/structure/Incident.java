package net.noahf.firegen.backend.database.structure;

import dev.morphia.annotations.*;
import dev.morphia.utils.IndexType;
import lombok.Getter;
import lombok.Setter;
import net.noahf.firegen.backend.database.structure.helper.IncidentSource;
import net.noahf.firegen.backend.database.structure.helper.IncidentStatus;
import net.noahf.firegen.backend.utils.Identifier;

import java.time.Instant;
import java.util.*;

@Entity(value = "incident")
@Indexes(value = @Index(fields = @Field(value = "incidentNumber", type = IndexType.DESC)))
@Getter @Setter
public class Incident {

    public Incident() {
        this.incidentNumber = new Random().nextInt(10000, 99999);
        this.incidentYear = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault()).get(Calendar.YEAR);
        this.fullId = this.incidentYear + "-" + String.format("%08d", this.incidentNumber);
    }

    @Id @Indexed
    public long incidentNumber;

    @Indexed
    public long incidentYear;

    public String fullId;

    private IncidentStatus incidentStatus = IncidentStatus.PENDING;

    public String incidentType = ">NEW<";

    public String incidentPriority = "";

    private IncidentSource incidentSource = IncidentSource.NINE_ONE_ONE;

    public Location location = new Location();

    public List<IncidentLogEntry> log = new ArrayList<>();

    public List<UnitAssignment> units = new ArrayList<>();

    public Instant created = Instant.now();
    public Instant closed = null;


    public void setIdentifier(Identifier id) {
        this.fullId = id.format();
        this.incidentNumber = id.id();
        this.incidentYear = id.year();
    }

    @Override
    public String toString() {
        return "Incident{" +
                "incidentNumber=" + incidentNumber +
                ", incidentYear=" + incidentYear +
                ", fullId='" + fullId + '\'' +
                ", incidentStatus=" + incidentStatus +
                ", incidentType='" + incidentType + '\'' +
                ", incidentPriority='" + incidentPriority + '\'' +
                ", callerType=" + incidentSource +
                ", location=" + location +
                ", log=" + log +
                ", created=" + created +
                ", closed=" + closed +
                '}';
    }
}

package net.noahf.firegen.discord.incidents.structure;

import jakarta.persistence.*;
import lombok.*;
import net.dv8tion.jda.api.entities.User;
import net.noahf.firegen.api.Contributor;
import net.noahf.firegen.api.incidents.IncidentLogEntry;
import net.noahf.firegen.api.incidents.IncidentPublishedStatus;
import net.noahf.firegen.api.incidents.IncidentTime;
import net.noahf.firegen.api.incidents.location.IncidentLocation;
import net.noahf.firegen.api.incidents.status.IncidentStatus;
import net.noahf.firegen.api.incidents.types.IncidentType;
import net.noahf.firegen.api.incidents.units.AssignmentStatus;
import net.noahf.firegen.api.incidents.units.Unit;
import net.noahf.firegen.api.incidents.units.UnitAssignment;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.incidents.IncidentManager;
import net.noahf.firegen.discord.incidents.messaging.IncidentMessagingService;
import net.noahf.firegen.discord.incidents.structure.location.IncidentLocationImpl;
import net.noahf.firegen.discord.incidents.structure.types.IncidentTypeImpl;
import net.noahf.firegen.discord.incidents.structure.types.IncidentTypeTagImpl;
import net.noahf.firegen.discord.incidents.structure.units.UnitAssignmentImpl;
import net.noahf.firegen.discord.incidents.structure.units.UnitImpl;
import net.noahf.firegen.discord.users.FireGenUser;
import net.noahf.firegen.discord.utilities.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RequiredArgsConstructor
@Getter @Setter @EqualsAndHashCode(of = {"id"})
@Entity @Table(name = "incidents")
public class IncidentImpl implements net.noahf.firegen.api.incidents.Incident {

    private final transient @Getter(value = AccessLevel.NONE) @Setter(value = AccessLevel.NONE)
            IncidentManager manager;

    @Getter
    @Id
    private final long id;

    @Enumerated
    private IncidentStatus status;

    @OneToOne(
            targetEntity = IncidentTypeImpl.class, cascade = CascadeType.ALL
    )
    @NotNull
    private IncidentType type;

    @OneToMany(
            targetEntity = UnitAssignmentImpl.class, cascade = CascadeType.ALL
    )
    private Set<UnitAssignment> unitAssignments;

    @NotNull
    @OneToOne(
            targetEntity = IncidentLocationImpl.class, cascade = CascadeType.ALL
    )
    private IncidentLocation location;

    @NotNull
    @OneToOne(
            targetEntity = IncidentTimeImpl.class, cascade = CascadeType.ALL
    )
    private IncidentTime time;

    @NotNull
    @Enumerated
    private IncidentPublishedStatus published;

    @OneToMany(targetEntity = IncidentLogEntryImpl.class, cascade = CascadeType.ALL)
    private List<IncidentLogEntry> log;

    @OneToMany(targetEntity = FireGenUser.class, cascade = CascadeType.ALL)
    private List<Contributor<?>> contributors;

    private transient @Getter IncidentMessagingService messagingService;

    public IncidentImpl() {
        this.manager = null;
        this.id = Long.MIN_VALUE;
    }

    public IncidentImpl(IncidentManager manager) {
        this.manager = manager;
        this.id = new Random(System.currentTimeMillis()).nextLong(1000000, 9999999);
        this.status = null;
        this.type = manager.getFireGenVariables().defaultType();
        this.location = new IncidentLocationImpl(new ArrayList<>());
        this.time = new IncidentTimeImpl(LocalDateTime.now());
        this.published = IncidentPublishedStatus.UNPUBLISHED;

        this.unitAssignments = new HashSet<>();
        this.log = new ArrayList<>();
        this.contributors = new ArrayList<>();

        this.messagingService = new IncidentMessagingService(this);
    }

    public void setTypeBySearch(String type) {
        IncidentType newType = manager.getTypeFromString(type);
        if (type.startsWith("custom:")) {
            type = type.substring("custom:".length()).toUpperCase();
            newType = new IncidentTypeImpl(type, IncidentTypeTagImpl.DEFAULT, 0);
        }

        if (type.startsWith("pub-")) {
            type = type.substring("pub-".length()).toUpperCase();
            this.setPublished(IncidentPublishedStatus.PUBLISHED);
            newType = manager.getTypeFromString(type);
        }

        if (newType == null) {
            throw new IllegalArgumentException("Expected a valid incident type from file, got '" + type + "'");
        }

        this.setType(newType);
    }

    @Override
    public void addContributor(Contributor<?> contributor) {
        if (this.contributors.contains(contributor)) {
            return;
        }
        this.contributors.add(contributor);
    }

    @Override
    public void setPublished(IncidentPublishedStatus newStatus) {
        this.published = newStatus;
        this.getMessagingService().notifyPublishChange();
    }

    public boolean isPublished() {
        return this.getPublished() == IncidentPublishedStatus.PUBLISHED;
    }

    public Contributor<User> addContributor(User user) {
        FireGenUser fireGenUser = Main.users.getByDiscord(user);
        this.addContributor(fireGenUser);
        return fireGenUser;
    }

    @Override
    public void addLog(Contributor<?> user, IncidentLogEntry.EntryType type, String log) {
        this.addLog(new IncidentLogEntryImpl(LocalDateTime.now(), user, log, type));
    }

    @Override
    public void addLog(IncidentLogEntry entry) {
        this.log.add(entry);
    }

    @Override
    public void injectLog(IncidentLogEntry entry) {
        for (int i = 0; i < this.log.size(); i++) {
            IncidentLogEntry element = this.log.get(i);
            if (element.getId() != entry.getId()) {
                continue;
            }

            this.log.set(i, entry);
            return;
        }
        throw new IllegalStateException("Narrative with ID '" + entry.getId() + "' does not exist in the incident with ID '" + this.getFormattedId() + "'");
    }

    @Override
    public void assignUnit(Unit unit, Contributor<?> contributor, AssignmentStatus assignment) {
        UnitAssignment unitAssignment = this.getUnitAssignmentFor(unit);
        if (unitAssignment == null) {
            unitAssignment = new UnitAssignmentImpl(this, unit, contributor);
            this.unitAssignments.add(unitAssignment);
        }

        unitAssignment.assign(contributor, assignment);
        this.refreshStatus();
    }

    public UnitAssignment getUnitAssignmentFor(Unit unit) {
        for (UnitAssignment assignment : this.unitAssignments) {
            if (assignment.getUnit().equals(unit)) {
                return assignment;
            }
        }

        return null;
    }

    public boolean containsUnit(Unit unit) {
        return Main.incidents.getAssignments().stream()
                .filter(u -> u.getIncident().equals(this))
                .anyMatch(u -> u.getUnit().equals(unit));
    }

    public void removeUnit(Unit unit) {
        this.removeUnit(unit, true);
    }

    private void removeUnit(Unit unit, boolean fromAll) {
        if (fromAll) {
            Main.incidents.getAssignments().removeIf(ua -> ua.getUnit().equals(unit) && ua.getIncident().equals(this));
        }

        ((UnitImpl)unit).getAssignments().removeIf(ua -> ua.getIncident().equals(this));

        this.unitAssignments.removeIf(a -> a.getUnit().equals(unit));
        this.refreshStatus();
    }

    public List<UnitAssignment> getSortedAssignments() {
        List<UnitAssignment> sorted = new ArrayList<>(this.getUnitAssignments());
        Collections.sort(sorted);
        return sorted;
    }

    public void refreshStatus() {
        if (this.status != null && !this.status.isInProgress()) {
            return;
        }

        if (unitAssignments.isEmpty()) {
            this.status = IncidentStatus.PENDING;
        } else {
            this.status = IncidentStatus.ACTIVE;
        }
    }

    @Override
    public void setLocation(@Nullable IncidentLocation location) {
        if (location == null) {
            location = new IncidentLocationImpl(new ArrayList<>());
        }
        this.location = location;
    }

    public String createInteractionIdString(String... commands) {
        // name of the command has to come first or this will not work
        return String.format(
                "firegen-%s-%s",
                this.getId(), String.join("-", commands)
        );
    }

    public List<IncidentLogEntry> getNarrative() {
        return this.getLog().stream().filter(IncidentLogEntry::isNarrative).toList();
    }

    public String getFormattedId() {
        return this.time.getDateTime().format(DateTimeFormatter.ofPattern("yyyy")) + "-" +
                this.getId();
    }

    /**
     * Post the incident changes to the saved messages. Used for updating subscribed servers with new information. <br>
     * <b>This method will block the main thread IF the incident has never been posted before.</b> <br>
     * This is required to ensure the order of the initial message and then edit message when the incident is created.
     */
    @Override
    public void update() {
        if (this.status == null) {
            return;
        }

        long ELAPSED_TIME_THRESHOLD = 25; // milliseconds
        long start = System.currentTimeMillis();

        this.messagingService.sendAll();

        long elapsed = System.currentTimeMillis() - start;
        if (elapsed > ELAPSED_TIME_THRESHOLD) {
            Log.info("Took " + elapsed + "ms to update messages.");
        }

        this.refreshStatus();
    }

}